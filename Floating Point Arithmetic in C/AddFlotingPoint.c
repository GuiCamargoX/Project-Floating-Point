#include <stdio.h>
#include <stdlib.h>
#include <math.h>

typedef struct{
unsigned int s,e,m;
}Float;

Float iniciaPonto(unsigned int sign, unsigned int exponent ,unsigned int mantissa){
    Float x;
    x.s=sign;
    x.e=exponent;

    mantissa= mantissa<<3;/*add 2guardBits and 1stackBit*/
    if(exponent > 0 && exponent < 255)
        mantissa += 0x4000000;/*add hide bit*/

    x.m=mantissa;
    return x;
}

	void Normalize(Float *resp){
		int maskH = 0b11111100000000000000000000000000;/*11111100_00000000_00000000_00000000*/
		int maskF = 0b00000011111111111111111111111111;/*00000011_11111111_11111111_11111111*/
		int hide = 	( resp->m & maskH )>>26;
		int positionMostSigSetBit = (int)log2( resp->m );/*find the position of the most significant set bit*/
        int desl = abs(positionMostSigSetBit - 26);/*27 represent the position of bitHide*/

		if(hide != 1){
			if(hide >=2 ){
                resp->m = resp->m >> desl;
                resp->e += desl;
			}
			if(hide <1){
                resp->m = resp->m <<desl;
                resp->e -=desl;
			}
		}
		return;
	}

	void RoundNear(Float *resp) {
		int bp = resp->m &  0b00000000000000000000000000000100;
		int bp1 = resp->m & 0b00000000000000000000000000000010;
		int bp2 = resp->m & 0b00000000000000000000000000000001;
		int EpsonMachine =  0b00000000000000000000000000001000;
		int RoundDown , RoundUp;

		RoundDown = resp->m & 0b11111111111111111111111111111000;
		RoundUp = RoundDown + EpsonMachine;

		if(	bp == 0 ){/*round down*/
			resp->m = RoundDown;
			return;
		}
		if( bp >= 1 && (bp1 >= 1 || bp2>=1) ){/*round up*/
			resp->m = RoundUp;
			return;
		}
		if(bp >= 1 && bp1 == 0 && bp2==0){/*tie*/
			int leastSignifUp = RoundUp & 0b00000000000000000000000000001000;

			if(leastSignifUp ==0 ){
				resp->m = RoundUp;
				return;
			}else{
				resp->m = RoundDown;
				return;
			}
		}
    }

	int add(Float x, Float y){
	    Float resp;
	    int respIEEE;
		int difExponents = abs(x.e - y.e);
		int memory=0;

		if(difExponents != 0){
			/*align the significands*/
			if(x.e < y.e ){/*chose the min number */
                    Float aux = y;
                    y = x;
                    x = aux;
			}
            for(int i=0; i<difExponents; i++){    /*act stickyBit*/
                memory = (y.m & 1) | memory;
                y.m = y.m>>1;
                y.e += 1;
				}
            y.m =memory| y.m;
		}
		//just add
		resp.s=0;
		resp.m = x.m + y.m;
		resp.e = x.e;

		Normalize(&resp);
        RoundNear(&resp);
		Normalize(&resp);

		resp.m = resp.m & 0b00000011111111111111111111111111;/*remove HideBit*/

        respIEEE = resp.s + (resp.e<<23) + (resp.m>>3);

		return respIEEE;
	}

int main(){
Float x = iniciaPonto(0,0x80,0);/*x = 2*/
Float y = iniciaPonto(0,0x80,0x400000);/*y= 3*/

/*more examples*/
Float z = iniciaPonto(0,0x7B,0x4CCCCD);/*z = 0.1*/
Float w = iniciaPonto(0,0x7C,0x4CCCCD);/*w= 0.2*/

printf( "\n0x%0x",add(z,w) );
printf( "\n0x%0x",add(z,y) );
printf( "\n0x%0x",add(x,y) );
printf( "\n0x%0x",add(x,w));

}
