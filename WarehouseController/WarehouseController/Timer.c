/*
 * Timer.c
 *
 * Created: 2019-04-10 오전 11:04:02
 *  Author: user
 */ 

#include <avr/io.h>
#define F_CPU 16000000UL
#include <util/delay.h>
#include <avr/interrupt.h> // interrupt 사용하기 위함
#include "FND4digit.h"
#include "Timer.h"


//FND4digit
extern char FND4digit_digit[4];
extern char FND[4];
extern char mode , cursor, long_key_flag;
extern int husuabi_bell_tune[], husuabi_bell_beat[], san_rabit_bell_tune[],san_rabit_bell_beat[];

//전역 변수는 초기화 안하면 0으로 초기화된다
volatile char time_flag = 1;
volatile int msec = 0, stop_msec = 0, music_i, long_key_msec = 0;				
volatile char i = 0, count;
volatile int beat, interval;																	

//isr은 가급적 빨리 실행해야 되기 때문에 딜레이 x, 함수호출x ...
ISR(TIMER0_COMPA_vect)						//interrupt 걸리면 실행하는 내용 //Interrupt Service Routine
{	
	if(count <= 10) {
		count++;
		msec++;
		if(msec >= 1000) {
			msec;
			time_flag = 1;
		}
	}
	else {
		msec++;
		if(msec >= 3000) {
			msec = 0;
			time_flag = 1;
		}
	}
	

	
	//FND를 실제로 출력시키는 
	i++;
 	
 	if(i >=4) i = 0;
	

	//끄고 데이터 주고 끄고
	FND_COM_PORT &= 0b00001111; //상위 4비트 끄고
	FND_shift_out(FND[i]);
	FND_COM_PORT |= FND4digit_digit[i];//< FND_shift_out보다 먼저하면 이전 데이터가 출력돼서 정확한 출력이 나오지 않음
	
} 

int Timer_main(void)
{
	
	while(1)
	{ 
		
	}
	
	return 0;
}

/* TIMSK0 – Timer/Counter Interrupt Mask Register
	어떤 interrupt 걸리게 할건지 정하는
	비교 매치 b, 비교 매치 a, overflow
*/
/* TIFR0 – Timer/Counter 0 Interrupt Flag Register
	interrupt 발생하면 flag 1
	자동으로 / 상태 register
*/

void Timer0_init(void)
{
	//타이머 초기화
	TCCR0A |= 1 << WGM01;					//0b00000010;  //CTC Mode
	TCCR0B |= (1 << CS00 | 1 << CS01);		//0b00000011;  //64분주 사용(하위 3bit : 011)
	OCR0A = 249;							//비교할 값 => (16,000,000 / 64) / 1000 = 250 (0 ~ "249")			//1ms 주기
	TIMSK0 |= 1 << OCIE0A;                  //0b00000010; //어떤 interrupt 걸리게 할건지 정하는	// OC0A 비교 매치 인터럽트 활성화
	
	return;
}

void Timer0_HC_SR04_init(void)
{
	TCCR0A |= 1 << WGM01;					//CTC 모드
	TCCR0B |= (1 << CS02) | (1 << CS00);	//1024분주
	OCR0A = 255;							//
	
	return;
}

void Timer0_init_CTC_outA(void) //A 출력을 쓰기위한 초기화 함수
{
	DDRD |= 1 << PORTD6;
	TCCR0A |= 1<< WGM01 | 1 <<COM0A0;
	TCCR0B |= 1 <<  CS00 | 1 <<CS01;
	
	OCR0A = 30; //4000Hz
	//OCR0A = 63; // 2000Hz
	//OCR0A = 124;							//(250 / 2) - 1 //한 주기를 1msec
	//OCR0A = 249;							//한 주기 2msec //500Hz
	//250Hz를 만들고싶다면? => OCR0A를 늘리면 오버플로우(8bit) => 분주를 늘린다
	//CS0x (분주)랑 OCR값을 이용해서 주기, 주파수 조정
	TIMSK0 |= 1 << OCIE0A;                  //0b00000010; //어떤 interrupt 걸리게 할건지 정하는	// OC0A 비교 매치 인터럽트 활성화
	
	return;
}

void Timer1_init_CTC_outA(void) //A 출력을 쓰기위한 초기화 함수
{
	DDRB |= 1 << PORTB1;
	TCCR1A |= 1 << COM1A0; //TOGGLE
	TCCR1B |= 1 << WGM12 | 1 <<  CS11;  //CTC 모드//64분주
	OCR1A = 0; // 소리 끄기
	
	//OCR0A = 30; //4000Hz
	//OCR0A = 63; // 2000Hz
	//OCR0A = 124;							//(250 / 2) - 1 //한 주기를 1msec
	//OCR0A = 249;							//한 주기 2msec //500Hz
	//250Hz를 만들고싶다면? => OCR0A를 늘리면 오버플로우(8bit) => 분주를 늘린다
	//CS0x (분주)랑 OCR값을 이용해서 주기, 주파수 조정
	//TIMSK0 |= 1 << OCIE0A;                  //0b00000010; //어떤 interrupt 걸리게 할건지 정하는	// OC0A 비교 매치 인터럽트 활성화
	
	return;
}