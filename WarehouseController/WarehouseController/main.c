/*
 * WarehouseController.c
 *
 * Created: 2019-06-11 오후 4:56:47
 * Author : tjoeun
 */ 

#include <avr/io.h>
#include <util/delay.h>
#define F_CPU 16000000UL
#include <avr/interrupt.h>
#include <stdlib.h>
#include <string.h>

#include "ADC.h"
#include "FND4digit.h"
#include "Keypad.h"
#include "Timer.h"
#include "Uart.h"

#define ROOM1_DESIRE_UP '1'
#define ROOM1_DESIRE_DOWN '2'
#define ROOM2_DESIRE_UP '3'
#define ROOM2_DESIRE_DOWN '4'
#define ROOM3_DESIRE_UP '5'
#define ROOM3_DESIRE_DOWN '6'

#define ROOM1_LED 3
#define ROOM2_LED 4
#define ROOM3_LED 5

#define ROOM1 1
#define ROOM2 2
#define ROOM3 3

void StorageController_Init(void);

extern char FND4digit_font[10];
extern volatile char time_flag;
extern volatile char RX_flag, RX_data, RX_cmd_count;
extern char buffer[COMMAND_MAX][BUFFER_MAX];
extern char FND[4];

char selected_room;
signed char room1_desire = 20, room2_desire = 20, room3_desire = 20, room1_current, room2_current, room3_current;

char long_key_flag;

int main(void)
{
	char cmd_idx = 0;
	char *command;
   StorageController_Init();
    while (1) 
    {
		
		CompareTemper();
		if(time_flag) {
			room1_current = Volt_to_temperature(ADC_converting_value(2));
			if(room1_current <= -30) room1_current = -30;
			else if(room1_current >= 30) room1_current = 30;
			
			room2_current = Volt_to_temperature(ADC_converting_value(1));
			if(room2_current <= -30) room2_current = -30;
			else if(room2_current >=  30) room2_current = 30;
			
			room3_current = Volt_to_temperature(ADC_converting_value(0));
			if(room3_current <= -30) room3_current = -30;
			else if(room3_current >=  30) room3_current = 30;
			
			printf("room1_current : %d\n", room1_current);
			_delay_us(10);
			printf("room2_current : %d\n", room2_current);
			_delay_us(10);
			printf("room3_current : %d\n", room3_current);
			
			printf("desire_temp : %d %d %d\n", room1_desire, room2_desire, room3_desire);
			time_flag = 0;
		}
		
		if(RX_cmd_count) {
			RX_cmd_count--;
			command = strtok(buffer[cmd_idx], " : ");
			if(!strcmp(command, "room1_desire")) { //strcmp() : 비교해서 같으면 return 0
				room1_desire = atoi(strtok(NULL, " : "));
				if(room1_desire <= -30) room1_desire = -30;
				else if(room1_desire >=  30) room1_desire = 30;
				printFND_temper(room1_desire, ROOM1);
				printf("room1_desire : %d\n", room1_desire);
			}
			else if(!strcmp(command, "room2_desire")) { 
				room2_desire = atoi(strtok(NULL, " : "));
				if(room2_desire <= -30) room2_desire = -30;
				else if(room2_desire >=  30) room2_desire = 30;
				printFND_temper(room2_desire, ROOM2);
				printf("room2_desire : %d\n", room2_desire);
			}
			else if(!strcmp(command, "room3_desire")) {
				room3_desire = atoi(strtok(NULL, " : "));
				if(room3_desire <= -30) room3_desire = -30;
				else if(room3_desire >=  30) room3_desire = 30;
				printFND_temper(room3_desire, ROOM3);
				printf("room3_desire : %d\n", room3_desire);
			}
			cmd_idx++;
			cmd_idx = cmd_idx % COMMAND_MAX;
		}
		
		
		if(long_key_flag) {
			_delay_us(200);
			if(Keyscan_sub() != 0)
			{
				
				
				switch(Keyscan_sub()) {
					case 1: SelectRoomProcess(); 
							long_key_flag = 0;
							break;
					case 2: TemperIncreaseProcess(selected_room); 
							long_key_flag = 0; 
							break;
					case 3: TemperDecreaseProcess(selected_room); 
							long_key_flag = 0;
							break;
					case 4: 
							switch(selected_room){
								case ROOM1: 
									printFND_temper(room1_current, ROOM1);
									long_key_flag = 0;
									break;
								case ROOM2: 
									printFND_temper(room2_current, ROOM2);
									long_key_flag = 0;
									break;
								case ROOM3: 
									printFND_temper(room3_current, ROOM3);
									long_key_flag = 0;
									break;
							}
							break;
					default: break;
				}
					
					
				
				long_key_flag = 0;
				
			
			}
		}
		else
		{
			if(!Keyscan_sub())
			{
				long_key_flag = 1;
			}
		}
		_delay_ms(10);
		
						
		
    }
}

void StorageController_Init(void) {
	
	Timer0_init();
	FND4digit_init_shiftR();
	
	Keypad_init();
	ADC_init();
	UART0_init(9600);
	
	DDRD |= 1 << ROOM1_LED; //LED RED
	PORTD &= ~(1 << ROOM1_LED);
	DDRB |= 1 << ROOM3_LED | 1 << ROOM2_LED; //LED BLUE , LED org
	PORTB &= ~(1 << ROOM3_LED | 1 << ROOM2_LED);
	
	
	sei();
	
	return;
}

void SelectRoomProcess(){
	
	selected_room++;
	if(selected_room > ROOM3) selected_room = ROOM1;
	
	if(selected_room == ROOM1) printFND_temper(room1_current, ROOM1);
	else if(selected_room == ROOM2) printFND_temper(room2_current, ROOM2);
	else if(selected_room == ROOM3) printFND_temper(room3_current, ROOM3);
	
	return;
}

void TemperIncreaseProcess(char room) {
	switch(room){
		case ROOM1: 
			room1_desire++;
			if(room1_desire >= 30) room1_desire = 30;
			printFND_temper(room1_desire, ROOM1);
			
			break;
		case ROOM2: room2_desire++;
			if(room2_desire >= 30) room2_desire = 30;
			printFND_temper(room2_desire, ROOM2);
		
			break;
		case ROOM3: room3_desire++;
			if(room3_desire >= 30) room3_desire = 30;
			printFND_temper(room3_desire, ROOM3);
			
			break;
	}
	printf("desire_temp : %d %d %d\n", room1_desire, room2_desire, room3_desire);
	return;
}

void TemperDecreaseProcess(char room){
	switch(selected_room){
		case ROOM1: room1_desire--;
			if(room1_desire <= -30) room1_desire = -30;
			printFND_temper(room1_desire, ROOM1);
		
			break;
		case ROOM2: room2_desire--;
			if(room2_desire <= -30) room2_desire = -30;
			printFND_temper(room2_desire, ROOM2);
			
			break;
		case ROOM3: room3_desire--;
			if(room3_desire <= -30) room3_desire = -30;
			printFND_temper(room3_desire, ROOM3);
			
			break;
	}
	printf("desire_temp : %d %d %d\n", room1_desire, room2_desire, room3_desire);
	return;
}

void printFND_temper(int temp, int room_number) {
	
	
	if(temp >= 0) {
		FND_update_value(temp);
		FND[2] = 255;
		//FND[3] = FND4digit_font[COOLING];
	}
	else{
		//temp = temp * -1; //양수 값으로 변경
		FND_update_value(temp * -1);
		FND[2] = ~(1 << FND_g);
		//FND[3] = FND4digit_font[COOLING];
	}
	if(room_number == ROOM1) FND[3] = FND4digit_font[ROOM1];
	else if(room_number == ROOM2) FND[3] = FND4digit_font[ROOM2];
	else if(room_number == ROOM3) FND[3] = FND4digit_font[ROOM3];
	
	return;
}

void CompareTemper(){
	
	if(room1_current > room1_desire) //RED
		PORTD |= 1 << ROOM1_LED;
	else
		PORTD &= ~(1 << ROOM1_LED);
	
	if(room2_current > room2_desire) //orange
		PORTB |= 1 << ROOM2_LED;
	else 
		PORTB &= ~(1 << ROOM2_LED);
		
	if(room3_current > room3_desire) //BLUE
		PORTB |= 1 << ROOM3_LED;
	else
		PORTB &= ~(1 << ROOM3_LED);
		
	return;
}