/**
 * A basic application to test reading/setting the IO I need.
 * Learning the domain.
 * 
 */

import java.io.*;
import java.text.*;
import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.labjack.LJUD;
import com.labjack.LJUDException;

public class Mine {

	public Mine() {
	}

	private void handleLJUDException(LJUDException e) {
		e.printStackTrace();
		if(e.getError() > LJUD.Errors.MIN_GROUP_ERROR.getValue()) {
			System.exit(-1);
		}
	}

	//Displays warning message if there is one. Error values < 0 are warnings
	//and do not cause a LJUDException in the LJUD class.
	private void checkForWarning(int error) {
		Pointer errorStringPtr = new Memory(256);
		if(error < 0) {
			LJUD.errorToString(error, errorStringPtr);
			System.out.println("Warning: " + errorStringPtr.getString(0).trim());
		}
	}

	public void runExample() {
		try {
			int intErrorcode;
			IntByReference refIOType = new IntByReference(0);
			IntByReference refChannel = new IntByReference(0);
			DoubleByReference refValue = new DoubleByReference(0);
			int intIOType = 0;
			int intChannel = 0;
			double dblValue = 0.0;
			double value0 = 9999, value1 = 9999, value2 = 9999;
			double valueDIBit = 9999, valueDIPort = 9999, valueCounter = 9999;
			int intHandle = 0;
			IntByReference refHandle = new IntByReference(0);
			IntByReference dummyInt = new IntByReference(0);
			DoubleByReference dummyDouble = new DoubleByReference(0.0);
			boolean isDone = false;

			NumberFormat formatter = new DecimalFormat("0.000");
			String line = "";
			BufferedReader br = new BufferedReader(
					new InputStreamReader(System.in));
	
			//Read and display the UD versions.
			System.out.println("UD Driver Version = " + formatter.format(LJUD.getDriverVersion()));
	
			//Open the first found LabJack U3.
			intErrorcode = LJUD.openLabJack(LJUD.Constants.dtU3, LJUD.Constants.ctUSB, "1", 1, refHandle);
			checkForWarning(intErrorcode);
			intHandle = refHandle.getValue();
	
			//Start by using the pin_configuration_reset IOType so that all
			//pin assignments are in the factory default condition.
			/* ePut add-go-get in one step. */
			LJUD.ePut(intHandle, LJUD.Constants.ioPIN_CONFIGURATION_RESET, 0, 0, 0);
	
			//First some configuration commands. These will be done with the ePut
			//function which combines the add/go/get into a single call.
	
			//Configure FIO0 and FIO3 as analog, all else as digital. That means we
			//will start from channel 0 and update all 16 flexible bits. We will
			//pass a value of b0000000000001111 or d15.
			LJUD.ePut(intHandle, LJUD.Constants.ioPUT_ANALOG_ENABLE_PORT, 0, 15, 16);
	
			//Set the timer/counter pin offset to 7, which will put the first
			//timer/counter on FIO7.
			LJUD.ePut(intHandle, LJUD.Constants.ioPUT_CONFIG,
					LJUD.Constants.chTIMER_COUNTER_PIN_OFFSET, 7, 0);
			
			//Enable Counter1 (FIO7).
			LJUD.ePut(intHandle, LJUD.Constants.ioPUT_COUNTER_ENABLE, 1, 1, 0);
			//LJUD.ePut(intHandle, LJUD.Constants.ioPUT_COUNTER_ENABLE, 0, 1, 0);
			
			
			//The following commands will use the add-go-get method to group
			//multiple requests into a single low-level function.
	
			//Request a single-ended reading from AIN0.
			LJUD.addRequest(intHandle, LJUD.Constants.ioGET_AIN, 0, 0, 0, 0);
			
			//Request a single-ended reading from AIN1.
			LJUD.addRequest(intHandle, LJUD.Constants.ioGET_AIN, 1, 0, 0, 0);
			
			//Request a reading from AIN2 using the Special range.
			LJUD.addRequest(intHandle, LJUD.Constants.ioGET_AIN_DIFF, 2, 0, 32, 0);

			//Set DAC0 to 4.5 volts.
			LJUD.addRequest(intHandle, LJUD.Constants.ioPUT_DAC, 0, 4.5, 0, 0);
			// /could not find a Read call for DAC, work around
			boolean dacOn = true;	

			//Set digital output FIO4 to output-high.
			LJUD.addRequest(intHandle, LJUD.Constants.ioPUT_DIGITAL_BIT, 4, 1,	0, 0);
			
			//Set digital output FIO5 to output-high.
			LJUD.addRequest(intHandle, LJUD.Constants.ioPUT_DIGITAL_BIT, 5, 1,	0, 0);
			
			//Read digital input FIO5.
			//LJUD.addRequest(intHandle, LJUD.Constants.ioGET_DIGITAL_BIT, 5, 0, 0, 0);
			
			//Read digital inputs FIO5 through FIO6.
			LJUD.addRequest(intHandle, LJUD.Constants.ioGET_DIGITAL_PORT, 5, 0, 2, 0);
			
			//Request the value of Counter1.
			LJUD.addRequest(intHandle, LJUD.Constants.ioGET_COUNTER, 1, 0, 0, 0);
			
			while(true) {
	
				/* 
				*	Lets toggle the voltage on DAC0 from 0 to 4.5 each run
				* 	LED connect GND - DAC0
				*/
				if(dacOn){
					// turn it off
					LJUD.addRequest(intHandle, LJUD.Constants.ioPUT_DAC, 0, 0.0, 0, 0);
					dacOn = false;
				}
				else{
					// turn it on
					LJUD.addRequest(intHandle, LJUD.Constants.ioPUT_DAC, 0, 4.5, 0, 0);
					dacOn = true;
				}	
			
				/*
				*	Try to read the switch
				*	N.O. switch GND & FIO5
				*/
				//Read digital input FIO5.
				LJUD.addRequest(intHandle, LJUD.Constants.ioGET_DIGITAL_BIT, 5, 0, 0, 0);
			
				/*
				*	Try to read the counter
				*	
				*/
				//Request the new value of Counter1.
				LJUD.addRequest(intHandle, LJUD.Constants.ioGET_COUNTER, 1, 0, 0, 0);
				
				//Try this
				//System.out.println("Counter out=" + eGet(intHandle, LJUD.Constants.ioGET_COUNTER, 1, 0, 0));
			
				//Execute the requests.
				LJUD.goOne(intHandle);

				// Can get anyting good from the counter here?
				
				
				
				
				
				
				//Get all the results. The input measurement results are stored.
				//All other results are for configuration or output requests so 
				//we are just checking whether there was an error.
				LJUD.getFirstResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
	
				isDone = false;
				try {
					while(!isDone) {
						intIOType = refIOType.getValue();
						intChannel = refChannel.getValue();
						dblValue = refValue.getValue();
						
						// View the values of each Result
						System.out.println("intIOType=" + intIOType +
											", intChannel=" + intChannel +
											", dblValue=" + dblValue );
											

						
						if(intIOType == LJUD.Constants.ioGET_AIN) {
							if(intChannel == 0)   
								value0 = dblValue;   	//AIN0
							if(intChannel == 1)
								value1 = dblValue;  	//AIN1
						}
		
						if(intIOType == LJUD.Constants.ioGET_AIN_DIFF)
							value2 = dblValue;   		//AIN2
		
						if(intIOType == LJUD.Constants.ioGET_DIGITAL_BIT)
							valueDIBit = dblValue;		//FIO5
		
						if(intIOType == LJUD.Constants.ioGET_DIGITAL_PORT)
							valueDIPort = dblValue;		//FIO5-FIO6
		
						if(intIOType == LJUD.Constants.ioGET_COUNTER)
							valueCounter = dblValue;	//Counter1 (FIO7)
		
						LJUD.getNextResult(intHandle, refIOType, refChannel, refValue, dummyInt, dummyDouble);
					}
				}
				catch(LJUDException le) {
					if(le.getError() == LJUD.Errors.NO_MORE_DATA_AVAILABLE.getValue()) {
						isDone = true;
					}
					else {
						throw le;
					}
				}
				
				/* 
				*	AIN0 has EI-1022 temp probe
				*	Red -> VS
				*	Black -> GND
				*	White -> AIN0
				*
				*	C = 100*volts - 273.15
				*	K = 100*volts
				*	F = ((100*volts)-273.15)*1.8 + 32
				*
				*	Note: this is a function for this:
				*		tcVoltsToTemp(int tcType, double tcVolts, double cjTempK, com.sun.jna.ptr.DoubleByReference pTCTempK) 
				*/
				
				// Convert to temperature       formatter.format(
				
				double degreesF = 0.0;
				double degressC = 0.0;
				
				degreesF = ((100 * value0 ) - 273.15) * 1.8 + 32;
				degressC = 100 * value0 - 273.15;
				
				//System.out.println("AIN0 (Temp) = " + String.format("%.2f", degreesF) + "degrees F, or " + String.format("%.2f", degressC) + " degrees C.");
				System.out.println("AIN0 (Temp) = " + formatter.format(degreesF) + " degrees F, or " + formatter.format(degressC) + " degrees C.");
				System.out.println("AIN1 = " + value1);
				System.out.println("AIN2 = " + value2);
				System.out.println("FIO5 = " + valueDIBit);
				System.out.println("FIO5-FIO6 = " + valueDIPort);	//Will read 3 (binary 11) if both lines are pulled-high as normal.
				System.out.println("Counter1 (FIO7) " + valueCounter);
	
				System.out.println("\nPress Enter to go again or (q) and Enter to quit");
				line = br.readLine().toUpperCase().trim();
				if(line.equals("Q")) {
					return;
				}
			}
		}
		catch(LJUDException le) {
			handleLJUDException(le);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Mine().runExample();
	}

}