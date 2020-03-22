/******************************************************************************

                            Online C Compiler.
                Code, Compile, Run and Debug C program online.
Write your code in this editor and press "Run" button to compile and execute it.

*******************************************************************************/

#include <stdio.h>

//Author Nandkumar G Dhavalikar
// TLV8 is format to store binary data in structured form into memory constraint devices.
// This TLV code is applicable for 8 bit controller only else segmentation fault may occur
// TLV8 Format 
//TLV8Header,Tag,Length,Value,Tag,Length,Value...Checksum
//- TLV8Header :2 byte (TLV Type,TagCount) ...type TLV8:0 ,TagCount: 1- 250 
//- [TAG] 1 byte (0-250 valid values) 255:deleted/unused tag , 254: 
//- [LEN] 1 byte , Indicate length of data bytes  
//- [VALUE] 1 byte
//- example TLV8(2): 0x00[TLV8],0x02[TagCount],0xFF[TAG],0x01[LEN],0x33[VAL],0x06[TAG],0x02[LEN],0x00[VAL],0x01[VAL],0xCC[CHK]
//- There  are 2 tags [TagCount]
//- 1st tag is deleted one and other new tag with same length can be written
//- CHK is Checksum [CHK]

// API 
unsigned char * TLV8StartAdd;
unsigned char TLV8Init(unsigned char * StructTLV8)
{
    TLV8StartAdd =  StructTLV8; // update starting address
    // varify all tag values
    // verify checksum
    return( !(*(StructTLV8+1)) && (*(StructTLV8+1) <= 250 ) && (!*(StructTLV8)) ); // validate tlv8 type and max index 1-250
}
// this updates checksum if TLV structure is valid
unsigned char TLVUpdateChecksum(void)
{
     unsigned char *TLV8NextTag = TLV8StartAdd,i,tlen,dlen,sum=0,error;
     sum+=*TLV8NextTag;TLV8NextTag++;
     sum+=*TLV8NextTag;
     tlen = *TLV8NextTag;TLV8NextTag++;
     if(!tlen) // tag length > 0
     {
        error |=1;
     }
     for(i=0; i<tlen ; i++)
     {
        sum+=*TLV8NextTag;
        if(*TLV8NextTag>250) // tag shuld not be > 250
        {
            error |=1;
        }
        TLV8NextTag++;
        sum+=*TLV8NextTag;
        dlen = *TLV8NextTag;TLV8NextTag++;
        if(!dlen) // data length canot be zero
        {
            error |=1;
        }
        while(dlen)
        {
            sum+=*TLV8NextTag;TLV8NextTag++;
            dlen--;
        }
     }
     *TLV8NextTag=sum;
     if(error)
     {
         printf("\nTLV db error\n");
     }
     return(error);
}

unsigned char TLV8UpdateTag(unsigned char tag, unsigned char len, unsigned char *val)
{
    // compare tag and legnt
    // if found update value, return sucess = 0
    // if tag and length not found through error, return TAG Not found = 10
  
    unsigned char *TLV8NextTag = (TLV8StartAdd+2),i;
    for(i=0; i<(*(TLV8StartAdd+1)) ; i++)
    {
      
        if( (*TLV8NextTag == tag) && ( *(TLV8NextTag+1) == len))
        {
            
           TLV8NextTag+=2; //point to value
           printf("\nUpdated Tag %x", tag);
           
           while(len)
           {
              *TLV8NextTag = *val;
              TLV8NextTag++;
              val++;
              len--;
         
           }
           TLVUpdateChecksum(); 
           return 0; 
        }
        else
        {
            TLV8NextTag+=(2 + *(TLV8NextTag+1) ); // point to next tag
        }
        
    }
  
    return 11;
}

unsigned int GetTLVSize(void)
{
     unsigned char *TLV8NextTag = TLV8StartAdd,i,tlen,dlen;
     unsigned short int sum=0;
     TLV8NextTag++;
     sum+=2;
     tlen = *TLV8NextTag;TLV8NextTag++;
     
     for(i=0; i<tlen ; i++)
     {
        TLV8NextTag++;
        dlen = *TLV8NextTag;
        sum+=(dlen+2);
        TLV8NextTag+=(dlen+1);// next tag
     }
     sum++;
     return sum;
}
void TLV8Print(void)
{
     unsigned char *TLV8NextTag = TLV8StartAdd,i,tlen,dlen;
     printf("\n\nTLV Tag Size:%d ,",GetTLVSize());
     
     printf("TLV Type:%d ,",*TLV8NextTag);TLV8NextTag++;
     tlen = *TLV8NextTag;TLV8NextTag++;
     printf("Max Tags:%d",tlen);
     for(i=0; i<tlen ; i++)
     {
        printf("\nTLV Tag:%x ,", *TLV8NextTag);TLV8NextTag++;
        dlen = *TLV8NextTag;TLV8NextTag++;
        printf("Len:%x ,",dlen);
        printf("Value:");
        while(dlen)
        {
            printf(" 0x%x,",*TLV8NextTag);TLV8NextTag++;
            dlen--;
        }
     }
     printf("\nChecksum :0x%x",*TLV8NextTag);
}
unsigned char TLV8DeleteTag(unsigned char tag,unsigned char len, unsigned char *val)
{
    // compare tag and legnt
    // if found delete tag , return sucess = 0
    // if tag and length not found through error,TAG Not found = 10
    // find deleted tag and update
    unsigned char *TLV8NextTag = (TLV8StartAdd+2),i;
    *TLV8NextTag = (TLV8StartAdd+2),i;
    for(i=0; i<(*(TLV8StartAdd+1)) ; i++)
    {
        if( (*TLV8NextTag == tag) && ( *(TLV8NextTag+1) == len))
        {
           *TLV8NextTag = 0xFF;
           TLV8NextTag+=2; //point to value
           printf("\nDeleted Tag %x", tag);
           TLVUpdateChecksum();
           return 0; 
        }
        else
        {
            TLV8NextTag+=(2 + *(TLV8NextTag+1) ); // point to next tag
        }
    }
    return 11;
}

unsigned char TLV8AddTag(unsigned char tag, unsigned char len, unsigned char *val)
{
    // compare tag and legnt
    // if found update value , return sucess = 0
    // if tag and length not found , serch for deleted tag of same length and update the TLV8
    // if not found deleted tag, No Space to Add Tag 11
    unsigned char *TLV8NextTag = (TLV8StartAdd+2),i;
    if(!TLV8UpdateTag(tag,len,val))
    {
        // find deleted tag and update
        *TLV8NextTag = (TLV8StartAdd+2),i;
        for(i=0; i<(*(TLV8StartAdd+1)) ; i++)
        {
            if( (*TLV8NextTag == 0xFF) && ( *(TLV8NextTag+1) == len))
            {
               *TLV8NextTag = tag;
               TLV8NextTag+=2; //point to value
               printf("\nUpdated Tag %x", tag);
               
               while(len)
               {
                  *TLV8NextTag = *val;
                  TLV8NextTag++;
                  val++;
                  len--;
             
               }
               TLVUpdateChecksum(); 
               return 0; 
            }
            else
            {
                TLV8NextTag+=(2 + *(TLV8NextTag+1) ); // point to next tag
            }
            
        }
        return 11;
    }
    else
    {
        return 0;
    }
}


unsigned char TLV8GetTagValue(unsigned char tag,unsigned char len, unsigned char *val)
{
    // compare tag and legnt
    // if found copy value , return sucess = 0
    // if tag and length not found , return TAG Not found = 10
     unsigned char *TLV8NextTag = (TLV8StartAdd+2),i;
    for(i=0; i<(*(TLV8StartAdd+1)) ; i++)
    {
      
        if( (*TLV8NextTag == tag) && ( *(TLV8NextTag+1) == len))
        {
            
           TLV8NextTag+=2; //point to value
           printf("\nFound Tag %x", tag);
           
           while(len)
           {
              *val=*TLV8NextTag;
              TLV8NextTag++;
              val++;
              len--;
           }
           TLVUpdateChecksum(); 
           return 0; 
        }
        else
        {
            TLV8NextTag+=(2 + *(TLV8NextTag+1) ); // point to next tag
        }
        
    }
     return 11; 
}



unsigned char TestTLV8[] = {0x00,0x02,0x05,0x01,0x33,0x06,0x02,0x00,0x01,0xCC};
int main()
{
    unsigned int a = 0x12345678;
    printf("TLV8 Init: %d\n",TLV8Init(TestTLV8));
    TLV8Print();
    TLVUpdateChecksum();
    TLV8Print();
    
    //TLV8UpdateTag(0x06,2,(unsigned char *)a);
    return 0;
}
