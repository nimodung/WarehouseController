/*
 * Keypad.c
 *
 * Created: 2019-04-16 오후 12:06:32
 *  Author: Kim Hee Ram
 */ 

#include <avr/io.h>
#define F_CPU 16000000UL
#include <util/delay.h>
#include <stdio.h>

#include "Uart.h"
#include "Keypad.h"



void Keypad_init(void)
{
	//Keypad_PORT_DDR |= 0b00000111; //포트D 하위 3bit 출력으로 설정
	//Keypad_PORT |= 0b00000111; // 하위 3bit 끄고 시작(풀업저항)
	Keypad_PIN_DDR &= 0b11110000; //포트B 하위 4bit 입력으로 설정
	Keypad_PIN_PORT |= 0b00001111;
		
	return;
}

char Keyscan_sub(void)
{
	if(!(Keypad_PIN & 0b00000001)) return 1; //키가 눌렸을때
	else if(!(Keypad_PIN & 0b00000010)) return 2;
	else if(!(Keypad_PIN & 0b00000100)) return 3;
	else if(!(Keypad_PIN & 0b00001000)) return 4;
	else return 0;
}

char Keyscan(void)
{
	for(int i = 0; i < 3; i++)
	{
		Keypad_PORT |= 0b00000111;
		Keypad_PORT &= ~(0b00000100 >> i); //clear &=
		_delay_ms(1);
		if(Keyscan_sub()) return 'A' + Keyscan_sub() + (i*4);
	}
	
	return 'A'; //아무것도 눌리지 않았음
}
/*
void key_test(void)
{
	
	if(Keyscan() != 'A')
	{
		_delay_us(200);
		if (Keyscan() != 'A')
		{
			switch(Keyscan()) {
				case 'B' : FND_update_value(1); break;
				case 'C' : FND_update_value(2); break;
				case 'D' :  FND_update_value(3); break;
				case 'E' :  FND_update_value(4); break;
				case 'F' : FND_update_value(5); break;
				case 'G': FND_update_value(6); break;
				case 'H': FND_update_value(7); break;
				case 'I': FND_update_value(8); break;
				case 'J': FND_update_value(9); break;
				case 'K': FND_update_value(10); break;
				case 'L': FND_update_value(11); break;
				case 'M': FND_update_value(12); break;
				case 'N': FND_update_value(13); break;
				case 'O': FND_update_value(14); break;
				case 'P': FND_update_value(15); break;
				case 'Q': FND_update_value(16); break;
			}
			
			long_key_flag = 0;
		}
		else
		{
			if(Keyscan() == 'A')
			{
				long_key_flag = 1;
			}
		}
	}


}*/