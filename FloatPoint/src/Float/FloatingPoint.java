package Float;

public class FloatingPoint {
	private int bits;
	private byte sign; 
	private int exponent;
	private int mantissa;
	public static final int bias = 127;
	
	public FloatingPoint(float p){
		bits = Float.floatToIntBits(p);/*float = 32bits*/
		sign     = (byte) ((bits>>31) & 0b00000000_00000000_00000000_00000001); 
		exponent =		   (bits>>23) & 0b00000000_00000000_00000000_11111111;
		mantissa = 		   (bits 	  & 0b00000000_01111111_11111111_11111111)<< 3;
		
		if(exponent > 0 && exponent < 255)
			mantissa += 0b00000100_00000000_00000000_00000000;/*add hide bit*/
		
	}
	
	public int getExponencialDecimal(){
		return this.exponent - bias ;
	}
	
	
	public FloatingPoint add(FloatingPoint p){
		//new FP
		
		int difExponents = Math.abs(this.exponent - p.exponent);
		int memory=0;
		
		if(difExponents != 0){
			//align the significands
			if(this.exponent > p.exponent ){
				
				for(int i=0; i<difExponents; i++){    /*act stickyBit*/
					memory = (p.mantissa & 1) | memory;
					p.mantissa = p.mantissa>>1;
					p.exponent += 1;
				}
				p.mantissa =memory| p.mantissa;
			}else{
				for(int i=0; i<difExponents; i++){    /*act stickyBit*/
					memory = (this.mantissa & 1) | memory;
					this.mantissa = this.mantissa>>1;
					this.exponent +=1;
				}
				this.mantissa =memory| this.mantissa;
			}
		}
		//just add
		this.mantissa = this.mantissa + p.mantissa;
		Normalize();
		RoundNear();
		Normalize();
		this.mantissa = this.mantissa & 0b00000011_11111111_11111111_11111111;
		
		ShowIEEEReprent();
		
		return this;
	}
	
	private void ShowIEEEReprent() {
		// TODO Auto-generated method stub
		String bitsInM = Integer.toBinaryString( this.mantissa>>3 );
		String bitsInE = Integer.toBinaryString( this.exponent );
		String bitsInS = Integer.toBinaryString((int)this.sign).substring(0);
		
		bitsInM = "000000000000000000000000000000000000000" + bitsInM;
		bitsInM = bitsInM.substring( bitsInM.length () - 23 );
		bitsInE = "000000000000000000000000000000000000000" + bitsInE;
		bitsInE = bitsInE.substring( bitsInE.length () - 8 );
		
		System.out.println( bitsInS + " | "+bitsInE+" |  "+bitsInM );
	}

	private void RoundNear() {
		int bp = this.mantissa &  0b00000000_00000000_00000000_00000100;
		int bp1 = this.mantissa & 0b00000000_00000000_00000000_00000010;
		int bp2 = this.mantissa & 0b00000000_00000000_00000000_00000001;
		int EpsonMachine = 0b00000000_00000000_00000000_00001000;
		int RoundDown , RoundUp;
		
		RoundDown = this.mantissa & 0b11111111_11111111_11111111_11111000;
		RoundUp = RoundDown + EpsonMachine;
		
		if(	bp == 0 ){/*round down*/
			this.mantissa = RoundDown;
			return;
		}
		if( bp >= 1 && bp1 >= 1 ){/*round up*/
			this.mantissa = RoundUp;
			return;
		}
		if(bp >= 1 && bp1 == 0 ){/*tie*/
			int leastSignifUp = RoundUp & 0b00000000_00000000_00000000_00001000;
			
			if(leastSignifUp ==0 ){
				this.mantissa = RoundUp;
				return;
			}else{
				this.mantissa = RoundDown;
				return;				
			}
		}
		
	}

	private void Normalize(){
		int maskH = 0b11111100_00000000_00000000_00000000;
		int maskF = 0b00000011_11111111_11111111_11111111;
		int hide = 		(this.mantissa & maskH)>>26;
		int fractPart =  this.mantissa & maskF;
		
		String hideN = Integer.toBinaryString(hide);
		String Real =  Integer.toBinaryString(fractPart);
		
		if(hide != 1){
			if(hide >=2 ){
				this.riseExponent(hideN);
			}
			if(hide <1)
				this.downExponent(Real);
		}
		this.mantissa = this.mantissa & 0b000000111_11111111_11111111_11111111 ;
		return;
	}

	private void downExponent(String part) {
		int occur = part.indexOf('1');
		int i = occur+1;
		
		this.mantissa = this.mantissa<<i;
		this.exponent -=i;
		
	}

	private void riseExponent(String hideB) {
		int occur = hideB.indexOf('1');
		int i = (hideB.length()-1) - occur;
		
		this.mantissa = this.mantissa>>i;
		this.exponent +=i;
	}
	
}
