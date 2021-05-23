/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package comp_arch_proj_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Armin Ekic and Luke Halverson
 */
public class Client {
    
    static int [] Register_File = new int[8]; //Register file for R0-R7
    //With this RAT we can check to see if the RAT is referenced by checking what
    //the value of the RAT entry is. If the entry is 0, it is pointing to the 
    //RF, otherwise 1-6 will indicate which RS value it's pointing to
    static int [] RAT = new int[8];
    static Linked_Queue<String> inst_Queue = new Linked_Queue<>(); //Linked queue to hold instructions
    static Linked_Queue<String> temp_Queue = new Linked_Queue<>(); //Temp queue to help with exceptions
    static int sim_clock = 0; //Holds the total simulated cycles
    static int current_clock = 0;
    static String decoded_instr; //Holds the decoded instr as a string
    static int int_instr; //holds the integer representation of the instruction
    static String string_num_of_instrs = null; //Holds the number of instrs being passed as a string
    static int int_num_of_instrs; //Holds the number of instrs being passed as an int
    static String num_cycles = ""; //Holds the number of cycles we will simulate
    static Integer RS1Result = null; 
    static Integer RS2Result = null; 
    static Integer RS3Result = null; 
    static Integer RS4Result = null; 
    static Integer RS5Result = null;
    static int has_issued;
    static Integer ROB1Result = null;
    static Integer ROB2Result = null;
    static Integer ROB3Result = null;
    static Integer ROB4Result = null;
    static Integer ROB5Result = null;
    static Integer ROB6Result = null;
    static int exceptionFlag; //Keeps track of whether an exception has been found
    static int instCommitCount = 0; //Keeps track of how many instructions have been committed
    static int numIssuedInst = 0; //Keeps track of how many instructions have been issued
    static int lowestROBNum = 1; //Keeps track of 
    
    /*
    This function will grab the instruction that is passed to it and decode it in order to
    put it into the instruction queue, and will also help determine how we want to load the
    instructions into the reservation stations
    */
    static String instruction_decode(String instr) {
         String decode = ""; //This will hold the instruction in a string representation
         
	//This will get each separate portion of the instruction
	char op_c = instr.charAt(0);
        char dest_reg_c = instr.charAt(1);
        char src1_c = instr.charAt(2);
        char src2_c = instr.charAt(3);
        
        //This will get the integer values of the instructions
	int op = Character.getNumericValue(op_c); //op code will be the first digit of the instr
	int dest_reg = Character.getNumericValue(dest_reg_c); //destination register is second digit of instr
	int src1 = Character.getNumericValue(src1_c); //source 1 is third digit of instr
	int src2 = Character.getNumericValue(src2_c); //source 2 is fourth digit of instr

	//This switch statement determines the type of instruction
	switch (op) {
            case 0:
		decode = decode + "Add ";
                break;
            case 1:
		decode = decode + "Sub ";
		break;
            case 2:
		decode = decode + "Multiply ";
		break;
            default:
		decode = decode + "Divide ";
		break;
	}

	//This switch statement will determine what value the register goes in
	switch (dest_reg) {
            case 0:
		decode = decode + "R0, ";
		break;
            case 1:
		decode = decode + "R1, ";
		break;
            case 2:
		decode = decode + "R2, ";
		break;
            case 3:
		decode = decode + "R3, ";
		break;
            case 4:
		decode = decode + "R4, ";
		break;
            case 5:
		decode = decode + "R5, ";
		break;
            case 6:
		decode = decode + "R6, ";
		break;
            default:
		decode = decode + "R7, ";
		break;
	}

	//This switch statement will determine the first source register
	switch (src1) {
            case 0:
		decode = decode + "R0, ";
		break;
            case 1:
		decode = decode + "R1, ";
		break;
            case 2:
		decode = decode + "R2, ";
		break;
            case 3:
		decode = decode + "R3, ";
		break;
            case 4:
		decode = decode + "R4, ";
		break;
            case 5:
		decode = decode + "R5, ";
		break;
            case 6:
		decode = decode + "R6, ";
		break;
            default:
		decode = decode + "R7, ";
		break;
	}

	//This switch statement will determine the second source register
	switch (src2) {
	case 0:
		decode = decode + "R0\n";
		break;
	case 1:
		decode = decode + "R1\n";
		break;
	case 2:
		decode = decode + "R2\n";
		break;
	case 3:
		decode = decode + "R3\n";
		break;
	case 4:
		decode = decode + "R4\n";
		break;
	case 5:
		decode = decode + "R5\n";
		break;
	case 6:
		decode = decode + "R6\n";
		break;
	default:
		decode = decode + "R7\n";
		break;
	}
	return decode; //Return the instruction string
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //This function will look through the RAT in order to make any necessary update
    //after the instruction has been broadcasted
    public static void updateRAT(int RS_entry){
        for(int i = 0; i <=7; i++){
            if(RAT[i] == RS_entry){
                RAT[i] = 0;
                break;
            }
        }
    }
    
    //This function will grab a certain RAT entry and set it to point to the RS at
    //a given entry
    public static void setRAT(int RAT_entry, int RS_tag){
        RAT[RAT_entry] = RS_tag;
    }
    
    //This function will update a given location with a given value
    public static void updateRF(int RF_entry, int value){
        Register_File[RF_entry] = value;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*
    //This function will put instructions into the queue
    static void add_iq(String instr){
        inst_Queue.enqueue(instr);
    }
    
    //This function will remove items from the instruction queue
    static String get_iq(){
        String inst;
        inst = inst_Queue.dequeue();
        return inst;
    }
    */
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //This is the class that will handle the Reservation stations more add/sub/mul/div
    private static class RS{
        private int position; //Holds the spot handled in the RS
        private int busy; //Keeps track of whether the entry is used
        private int op; //Keeps track of the instruction in the entry
        private int tag1; //Keeps track of tag 1
        private int tag2; //Keeps track of tag 2
        private int val1; //Keeps track of the first value
        private int val2; //Keeps track of the second value
        private int checkClock; //Keeps track of what can be done in a given cycle
        private int ROBdest; //Hols the position of the ROB we're writing the ROB to
        
        //Constructor method for creating an RS spot
        public RS(int position, int busy, int op, int tag1, int tag2, int val1, int val2, int checkClock, int ROBdest){
            this.position = position;
            this.busy = busy;
            this.op = op;
            this.tag1 = tag1;
            this.tag2 = tag2;
            this.val1 = val1;
            this.val2 = val2;
            this.checkClock = checkClock;
            this.ROBdest = ROBdest;
        } 

        //This function sets the position bit of an entry
        public void setPosition(int pos){
            position = pos;
        } 
        
        //This function sets the busy bit of an entry
        public void setBusy(int busy){
            this.busy = busy;
        }
        
        //This function sets the op code of an entry
        public void setOp(int op){
            this.op = op;
        }
        
        //This function sets tag1 of an entry
        public void setTag1(int tag){
            tag1 = tag;
        }
        
        //This function sets tag2 of an entry
        public void setTag2(int tag){
            tag2 = tag;
        }
        
        //This function sets val1 of an entry
        public void setVal1(int val){
            val1 = val;
        }
        
        //This function sets val2 of an entry
        public void setVal2(int val){
            val2 = val;
        }
        
        public void setCheckClock(int cc){
            checkClock = cc;
        }
        
        public void setROBdest(int ROBdest){
            this.ROBdest = ROBdest;
        }
        
        //This function will tell us whether the RS entry is free
        public boolean isBusy(){
            if(busy == 1)
                return true;
            else
                return false;
        }
        
        //This function will get the position of an entry
        public int getPosition(){
            return position;
        }
        
        //This function will get the op code of an entry
        public int getOp(){
            return op;
        }
        
        //This function will get tag1 of an entry
        public int getTag1(){
            return tag1;
        }
        
        //This function will get tag2 of an entry
        public int getTag2(){
            return tag2;
        }
        
        //This function will get val1 of an entry
        public int getVal1(){
            return val1;
        }
        
        //This function will get val2 of an entry
        public int getVal2(){
            return val2;
        }
        
        public int getCheckClock(){
            return checkClock;
        }
        
        public int getROBdest(){
            return ROBdest;
        }
    }
    
    //These will be the reservation station entries, all values initialized to 0
    static RS RS1 = new Client.RS(1, 0, 0, 0, 0, 0, 0, 0, 0); 
    static RS RS2 = new Client.RS(2, 0, 0, 0, 0, 0, 0, 0, 0);
    static RS RS3 = new Client.RS(3, 0, 0, 0, 0, 0, 0, 0, 0);
    static RS RS4 = new Client.RS(4, 0, 0, 0, 0, 0, 0, 0, 0);
    static RS RS5 = new Client.RS(5, 0, 0, 0, 0, 0, 0, 0, 0);
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static class ROB{
        private int position; //Holds the position that a given entry is at
        private int type; //Holds the type of instruction for a given entry
        private int dest; //Holds the destination register for the given instruction
        private int val; //Holds the value of the given instruction
        private int done; //Keeps track of whether or not the ROB position is complete
        private int exception; //Keeps track of whether or not the entry caused an exception
        private int checkClock; //Keeps track of when we can broadcast and dispatch the ROB entry
        private int compCost; //Keeps track of how long we need to wait before broadcasting the ROB entry
        private int instNum; //This keeps track of what instruction is being put in the ROB
        
        public ROB(int position, int type, int dest, int val, int done, int exception, int checkClock, int compCost, int instNum){
            this.position = position;
            this.type = type;
            this.dest = dest;
            this.val = val;
            this.done = done;
            this.exception = exception;
            this.checkClock = checkClock;
            this.compCost = compCost;
            this.instNum = instNum;
        }
        
        public void setPosition(int position){
            this.position = position;
        }
        public void setType(int type){
            this.type = type;
        }
        public void setDest(int dest){
            this.dest = dest;
        }
        public void setVal(int val){
            this.val = val;
        }
        public void setDone(int done){
            this.done = done;
        }
        public void setException(int exception){
            this.exception = exception;
        }
        public void setCheckClock(int checkClock){
            this.checkClock = checkClock;
        }
        public int getPosition(){
            return position;
        }
        public int getType(){
            return type;
        }
        public int getDest(){
            return dest;
        }
        public int getVal(){
            return val;
        }
        public int getDone(){
            return done;
        }
        public int getException(){
            return exception;
        }
        public int getCheckClock(){
            return checkClock;
        }
        
        static ROB ROB1 = new Client.ROB(1, 4, 0, 0, 1, 0, 0, 0, 0);
        static ROB ROB2 = new Client.ROB(2, 4, 0, 0, 1, 0, 0, 0, 0);
        static ROB ROB3 = new Client.ROB(3, 4, 0, 0, 1, 0, 0, 0, 0);
        static ROB ROB4 = new Client.ROB(4, 4, 0, 0, 1, 0, 0, 0, 0);
        static ROB ROB5 = new Client.ROB(5, 4, 0, 0, 1, 0, 0, 0, 0);
        static ROB ROB6 = new Client.ROB(6, 4, 0, 0, 1, 0, 0, 0, 0);
        
        public static void flushROB(){
            ROB1.type = 4;
            ROB1.dest = 0;
            ROB1.val = 0;
            ROB1.done = 1;
            ROB1.exception = 0;
            ROB1.checkClock = 0;
            ROB1.compCost = 0;
            ROB1.instNum = 0;
            
            ROB2.type = 4;
            ROB2.dest = 0;
            ROB2.val = 0;
            ROB2.done = 1;
            ROB2.exception = 0;
            ROB2.checkClock = 0;
            ROB2.compCost = 0;
            ROB2.instNum = 0;
            
            ROB3.type = 4;
            ROB3.dest = 0;
            ROB3.val = 0;
            ROB3.done = 1;
            ROB3.exception = 0;
            ROB3.checkClock = 0;
            ROB3.compCost = 0;
            ROB3.instNum = 0;
            
            ROB4.type = 4;
            ROB4.dest = 0;
            ROB4.val = 0;
            ROB4.done = 1;
            ROB4.exception = 0;
            ROB4.checkClock = 0;
            ROB4.compCost = 0;
            ROB4.instNum = 0;
            
            ROB5.type = 4;
            ROB5.dest = 0;
            ROB5.val = 0;
            ROB5.done = 1;
            ROB5.exception = 0;
            ROB5.checkClock = 0;
            ROB5.compCost = 0;
            ROB5.instNum = 0;
            
            ROB6.type = 4;
            ROB6.dest = 0;
            ROB6.val = 0;
            ROB6.done = 1;
            ROB6.exception = 0;
            ROB6.checkClock = 0;
            ROB6.compCost = 0;
            ROB6.instNum = 0;
            
            for(int i = 0; i <= 7; i++)
                RAT[i] = 0;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    //This method will take the instruction from the queue and issue into the RS, no arguments because
    //everything will naturally go in order
    
    public static void issue(RS entry, String instr){
        String temp_instr = instr;
        String [] arr = temp_instr.split(" ");
        
        switch(arr[0]){
            case "Add":
                entry.op = 0;
                break;
            case "Sub":
                entry.op = 1;
                break;
            case "Multiply":
                entry.op = 2;
                break;
            default:
                 entry.op = 3;
                 break;
        }
        
        //This switch will take care of issuing the src1 value from the instruction
        switch(arr[2]){
            case "R0,":
                if(RAT[0] != 0)
                    entry.tag1 = RAT[0];
                else
                    entry.val1 = Register_File[0];
                break;
            case "R1,":
                if(RAT[1] != 0)
                    entry.tag1 = RAT[1];
                else
                    entry.val1 = Register_File[1];
                break;
            case "R2,":
                if(RAT[2] != 0)
                    entry.tag1 = RAT[2];
                else
                    entry.val1 = Register_File[2];
                break;
            case "R3,":
                if(RAT[3] != 0)
                    entry.tag1 = RAT[3];
                else
                    entry.val1 = Register_File[3];
                break;
            case "R4,":
                if(RAT[4] != 0)
                    entry.tag1 = RAT[4];
                else
                    entry.val1 = Register_File[4];
                break;
            case "R5,":
                if(RAT[5] != 0)
                    entry.tag1 = RAT[5];
                else
                    entry.val1 = Register_File[5];
                break;
            case "R6,":
                if(RAT[6] != 0)
                    entry.tag1 = RAT[6];
                else
                    entry.val1 = Register_File[6];
                break;
            default:
                 if(RAT[7] != 0)
                    entry.tag1 = RAT[7];
                else
                    entry.val1 = Register_File[7];
                break;
        }
        
        //This switch takes care of issuing the src2 value from the instruction
        switch(arr[3]){
            case "R0\n":
                if(RAT[0] != 0)
                    entry.tag2 = RAT[0];
                else
                    entry.val2 = Register_File[0];
                break;
            case "R1\n":
                if(RAT[1] != 0)
                    entry.tag2 = RAT[1];
                else
                    entry.val2 = Register_File[1];
                break;
            case "R2\n":
                if(RAT[2] != 0)
                    entry.tag2 = RAT[2];
                else
                    entry.val2 = Register_File[2];
                break;
            case "R3\n":
                if(RAT[3] != 0)
                    entry.tag2 = RAT[3];
                else
                    entry.val2 = Register_File[3];
                break;
            case "R4\n":
                if(RAT[4] != 0)
                    entry.tag2 = RAT[4];
                else
                    entry.val2 = Register_File[4];
                break;
            case "R5\n":
                if(RAT[5] != 0)
                    entry.tag2 = RAT[5];
                else
                    entry.val2 = Register_File[5];
                break;
            case "R6\n":
                if(RAT[6] != 0)
                    entry.tag2 = RAT[6];
                else
                    entry.val2 = Register_File[6];
                break;
            default:
                 if(RAT[7] != 0)
                    entry.tag2 = RAT[7];
                else
                    entry.val2 = Register_File[7];
                break;
        }
        
        //This will take care of pairing the RS entry with a ROB entry
        while(true){
            if(ROB.ROB1.done == 1 && ROB.ROB1.type == 4){
                entry.ROBdest = 1;
                ROB.ROB1.instNum = numIssuedInst;
                ROB.ROB1.type = entry.op;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB1.dest = 0;
                        RAT[0] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB1.dest = 1;
                        RAT[1] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB1.dest = 2;
                        RAT[2] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB1.dest = 3;
                        RAT[3] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB1.dest = 4;
                        RAT[4] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB1.dest = 5;
                        RAT[5] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB1.dest = 6;
                        RAT[6] = 1;
                        ROB.ROB1.done = 0;
                        break;
                    default:
                        ROB.ROB1.dest = 7;
                        RAT[7] = 1;
                        ROB.ROB1.done = 0;
                        break;
                }
                break;
            }
            else if(ROB.ROB2.done == 1 && ROB.ROB2.type == 4){
                entry.ROBdest = 2;
                ROB.ROB2.instNum = numIssuedInst;
                ROB.ROB2.type = entry.op;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB2.dest = 0;
                        RAT[0] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB2.dest = 1;
                        RAT[1] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB2.dest = 2;
                        RAT[2] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB2.dest = 3;
                        RAT[3] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB2.dest = 4;
                        RAT[4] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB2.dest = 5;
                        RAT[5] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB2.dest = 6;
                        RAT[6] = 2;
                        ROB.ROB2.done = 0;
                        break;
                    default:
                        ROB.ROB2.dest = 7;
                        RAT[7] = 2;
                        ROB.ROB2.done = 0;
                        break;
                }
                break;
            }
            else if(ROB.ROB3.done == 1 && ROB.ROB3.type == 4){
                entry.ROBdest = 3;
                ROB.ROB3.instNum = numIssuedInst;
                ROB.ROB3.type = entry.op;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB3.dest = 0;
                        RAT[0] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB3.dest = 1;
                        RAT[1] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB3.dest = 2;
                        RAT[2] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB3.dest = 3;
                        RAT[3] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB3.dest = 4;
                        RAT[4] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB3.dest = 5;
                        RAT[5] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB3.dest = 6;
                        RAT[6] = 3;
                        ROB.ROB3.done = 0;
                        break;
                    default:
                        ROB.ROB3.dest = 7;
                        RAT[7] = 3;
                        ROB.ROB3.done = 0;
                        break;
                }
                break;
            }
            else if(ROB.ROB4.done == 1 && ROB.ROB4.type == 4){
                entry.ROBdest = 4;
                ROB.ROB4.instNum = numIssuedInst;
                ROB.ROB4.type = entry.op;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB4.dest = 0;
                        RAT[0] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB4.dest = 1;
                        RAT[1] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB4.dest = 2;
                        RAT[2] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB4.dest = 3;
                        RAT[3] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB4.dest = 4;
                        RAT[4] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB4.dest = 5;
                        RAT[5] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB4.dest = 6;
                        RAT[6] = 4;
                        ROB.ROB4.done = 0;
                        break;
                    default:
                        ROB.ROB4.dest = 7;
                        RAT[7] = 4;
                        ROB.ROB4.done = 0;
                        break;
                }
                break;
            }
            else if (ROB.ROB5.done == 1 && ROB.ROB5.type == 4){
                entry.ROBdest = 5;
                ROB.ROB5.instNum = numIssuedInst;
                ROB.ROB5.type = entry.op;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB5.dest = 0;
                        RAT[0] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB5.dest = 1;
                        RAT[1] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB5.dest = 2;
                        RAT[2] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB5.dest = 3;
                        RAT[3] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB5.dest = 4;
                        RAT[4] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB5.dest = 5;
                        RAT[5] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB5.dest = 6;
                        RAT[6] = 5;
                        ROB.ROB5.done = 0;
                        break;
                    default:
                        ROB.ROB5.dest = 7;
                        RAT[7] = 5;
                        ROB.ROB5.done = 0;
                        break;
                }
                break;
            }
            else{
                ROB.ROB6.type = entry.op;
                entry.ROBdest = 6;
                ROB.ROB6.instNum = numIssuedInst;
                switch(arr[1]){
                    case "R0,":
                        ROB.ROB6.dest = 0;
                        RAT[0] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R1,":
                        ROB.ROB6.dest = 1;
                        RAT[1] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R2,":
                        ROB.ROB6.dest = 2;
                        RAT[2] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R3,":
                        ROB.ROB6.dest = 3;
                        RAT[3] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R4,":
                        ROB.ROB6.dest = 4;
                        RAT[4] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R5,":
                        ROB.ROB6.dest = 5;
                        RAT[5] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    case "R6,":
                        ROB.ROB6.dest = 6;
                        RAT[6] = 6;
                        ROB.ROB6.done = 0;
                        break;
                    default:
                        ROB.ROB6.dest = 7;
                        RAT[7] = 6;
                        ROB.ROB6.done = 0;
                        break;
                }
                break;
            }
        }
        
        /*
        //This switch will point the RAT to the required RS entry based on the dest_reg
        switch(arr[1]){
            case "R0,":
                RAT[0] = entry.position;
                break;
            case "R1,":
                RAT[1] = entry.position;
                break;
            case "R2,":
                RAT[2] = entry.position;
                break;
            case "R3,":
                RAT[3] = entry.position;
                break;
            case "R4,":
                RAT[4] = entry.position;
                break;
            case "R5,":
                RAT[5] = entry.position;
                break;
            case "R6,":
                RAT[6] = entry.position;
                break;
            default:
                 RAT[7] = entry.position;
                break;
        }
        */
        entry.busy = 1;
    }
    
    //This function will dispatch the instruction from the given entry 
    public static int dispatch(RS entry){
        int result;
        if(entry.op == 0 || entry.op == 1){
            result = AddSub(entry.op, entry.val1, entry.val2);
            switch(entry.ROBdest){
                case 1:
                    ROB.ROB1.compCost = 2;
                    break;
                case 2:
                    ROB.ROB2.compCost = 2;
                    break;
                case 3:
                    ROB.ROB3.compCost = 2;
                    break;
                case 4:
                    ROB.ROB4.compCost = 2;
                    break;
                case 5:
                    ROB.ROB5.compCost = 2;
                    break;
                default:
                    ROB.ROB6.compCost = 2;
                    break;
            }
        }
        else{
            result = MulDiv(entry.op, entry.val1, entry.val2);
            if(entry.op == 2){
                switch(entry.ROBdest){
                case 1:
                    ROB.ROB1.compCost = 10;
                    break;
                case 2:
                    ROB.ROB2.compCost = 10;
                    break;
                case 3:
                    ROB.ROB3.compCost = 10;
                    break;
                case 4:
                    ROB.ROB4.compCost = 10;
                    break;
                case 5:
                    ROB.ROB5.compCost = 10;
                    break;
                default:
                    ROB.ROB6.compCost = 10;
                    break;
                }
            }
            else{
                switch(entry.ROBdest){
                case 1:
                    ROB.ROB1.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB1.compCost = 38;
                        ROB.ROB1.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                case 2:
                    ROB.ROB2.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB2.compCost = 38;
                        ROB.ROB2.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                case 3:
                    ROB.ROB3.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB3.compCost = 38;
                        ROB.ROB3.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                case 4:
                    ROB.ROB4.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB4.compCost = 38;
                        ROB.ROB4.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                case 5:
                    ROB.ROB5.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB5.compCost = 38;
                        ROB.ROB5.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                default:
                    ROB.ROB6.compCost = 40;
                    if(exceptionFlag == 1){
                        ROB.ROB6.compCost = 38;
                        ROB.ROB6.exception = 1;
                        exceptionFlag = 0;
                    }
                    break;
                }
            }
        }
        entry.busy = 0;
        return result;
    }
    
    //This function will update any RS tags as necessary and update ROB value/done
    public static void broadcast(ROB entry, Integer result){
        //This chunk of code will reset and necessary tags in the RS and write the correct values
        if(RS1.tag1 == entry.position){
            RS1.val1 = result;
            RS1.tag1 = 0;
        }
        if(RS1.tag2 == entry.position){
            RS1.val2 = result;
            RS1.tag2 = 0;
        }
        if(RS2.tag1 == entry.position){
            RS2.val1 = result;
            RS2.tag1 = 0;
        }
        if(RS2.tag2 == entry.position){
            RS2.val2 = result;
            RS2.tag2 = 0;
        }
        if(RS3.tag1 == entry.position){
            RS3.val1 = result;
            RS3.tag1 = 0;
        }
        if(RS3.tag2 == entry.position){
            RS3.val2 = result;
            RS3.tag2 = 0;
        }
        if(RS4.tag1 == entry.position){
            RS4.val1 = result;
            RS4.tag1 = 0;
        }
        if(RS4.tag2 == entry.position){
            RS4.val2 = result;
            RS4.tag2 = 0;
        }
        if(RS5.tag1 == entry.position){
            RS5.val1 = result;
            RS5.tag1 = 0;
        }
        if(RS5.tag2 == entry.position){
            RS5.val2 = result;
            RS5.tag2 = 0;
        }
        
        entry.val = result;
        
        for(int i = 0; i <= 7; i++){
            if(i == entry.dest){
                Register_File[i] = result;
                if(RAT[i] == entry.position)
                    RAT[i] = 0;
            }
        }
        entry.done = 1;  
    }
    
    //This function will commit any values from the ROB when they're ready
    public static void commit(ROB entry){
        for(int i = 0; i <=7; i++){
            entry.instNum = 0;
            if(entry.dest == i){
                Register_File[i] = entry.val;
                if(entry.position == RAT[i])
                    RAT[i] = 0;
                break;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    
    //Add/Sub unit
    public static int AddSub(int op, int val1, int val2){
        if(op == 0)
            return val1 + val2;
        else 
            return val1 - val2;
    }
    
    //Mul/Div unit
    public static int MulDiv(int op, int val1, int val2){
        if(op == 2){
            return val1 * val2;
        }
        else{
            try{
            return val1 / val2;
            }
            catch(ArithmeticException ae){
                exceptionFlag = 1;
                return 0;
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void TOM(){
        for(int i = 0; i < int_num_of_instrs + 1; i++){
            if(!inst_Queue.isEmpty()){
                String instruction = inst_Queue.dequeue();
                String [] temp = instruction.split(" ");
                has_issued = 0;
                if(temp[0].equals("Add") || temp[0].equals("Sub")){
                    if(!RS1.isBusy() && RS1.checkClock != current_clock){
                        numIssuedInst++;
                        issue(RS1, instruction);
                        RS1.checkClock = current_clock;
                        System.out.println("issuing " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        has_issued = 1;
                        break;
                    }
                    else if(!RS2.isBusy() && RS2.checkClock != current_clock){
                        numIssuedInst++;
                        issue(RS2, instruction);
                        RS2.checkClock = current_clock;
                        System.out.println("issuing " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        has_issued = 1;
                        break;
                    }
                    else if (!RS3.isBusy() && RS3.checkClock != current_clock){
                        numIssuedInst++;
                        issue(RS3, instruction);
                        RS3.checkClock = current_clock;
                        System.out.println("issuing " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        has_issued = 1;
                        break;
                    }
                }
                else{
                    if(!RS4.isBusy() && RS4.checkClock != current_clock){
                        numIssuedInst++;
                        issue(RS4, instruction);
                        RS4.checkClock = current_clock;
                        System.out.println("issuing " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        has_issued = 1;
                        break;
                    }
                    else if(!RS5.isBusy() && RS5.checkClock != current_clock){
                        numIssuedInst++;
                        issue(RS5, instruction);
                        RS5.checkClock = current_clock;
                        System.out.println("issuing " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        has_issued = 1;
                        break;
                    }
                }
                if(has_issued != 1)
                        inst_Queue.enqueue(instruction);
            }
        }
        if(RS1.getTag1() == 0 && RS1.getTag2() == 0 && RS1.checkClock != current_clock && RS1.isBusy()){
            //RS1Result = dispatch(RS1);
            switch(RS1.ROBdest){
                case 1:
                    ROB1Result = dispatch(RS1);
                    ROB.ROB1.checkClock = current_clock;
                    break;
                case 2:
                    ROB2Result = dispatch(RS1);
                    ROB.ROB2.checkClock = current_clock;
                    break;
                case 3:
                    ROB3Result = dispatch(RS1);
                    ROB.ROB3.checkClock = current_clock;
                    break;
                case 4:
                    ROB4Result = dispatch(RS1);
                    ROB.ROB4.checkClock = current_clock;
                    break;
                case 5:
                    ROB5Result = dispatch(RS1);
                    ROB.ROB5.checkClock = current_clock;
                    break;
                default:
                    ROB6Result = dispatch(RS1);
                    ROB.ROB6.checkClock = current_clock;
                    break;
            }
            System.out.println("Dispatching RS1\n");
        }
        if(RS2.getTag1() == 0 && RS2.getTag2() == 0 && RS2.checkClock != current_clock && RS2.isBusy()){
            //RS2Result = dispatch(RS2);
            switch(RS2.ROBdest){
                case 1:
                    ROB1Result = dispatch(RS2);
                    ROB.ROB1.checkClock = current_clock;
                    break;
                case 2:
                    ROB2Result = dispatch(RS2);
                    ROB.ROB2.checkClock = current_clock;
                    break;
                case 3:
                    ROB3Result = dispatch(RS2);
                    ROB.ROB3.checkClock = current_clock;
                    break;
                case 4:
                    ROB4Result = dispatch(RS2);
                    ROB.ROB4.checkClock = current_clock;
                    break;
                case 5:
                    ROB5Result = dispatch(RS2);
                    ROB.ROB5.checkClock = current_clock;
                    break;
                default:
                    ROB6Result = dispatch(RS2);
                    ROB.ROB6.checkClock = current_clock;
                    break;
            }
            System.out.println("Dispatching RS2\n");
        }
        if(RS3.getTag1() == 0 && RS3.getTag2() == 0 && RS3.checkClock != current_clock && RS3.isBusy()){
            //RS3Result = dispatch(RS3);
            switch(RS3.ROBdest){
                case 1:
                    ROB1Result = dispatch(RS3);
                    ROB.ROB1.checkClock = current_clock;
                    break;
                case 2:
                    ROB2Result = dispatch(RS3);
                    ROB.ROB2.checkClock = current_clock;
                    break;
                case 3:
                    ROB3Result = dispatch(RS3);
                    ROB.ROB3.checkClock = current_clock;
                    break;
                case 4:
                    ROB4Result = dispatch(RS3);
                    ROB.ROB4.checkClock = current_clock;
                    break;
                case 5:
                    ROB5Result = dispatch(RS3);
                    ROB.ROB5.checkClock = current_clock;
                    break;
                default:
                    ROB6Result = dispatch(RS3);
                    ROB.ROB6.checkClock = current_clock;
                    break;
            }
            System.out.println("Dispatching RS3\n");
            //RS3.checkClock = current_clock;
        }
        if(RS4.getTag1() == 0 && RS4.getTag2() == 0 && RS4.checkClock != current_clock && RS4.isBusy()){
            //RS4Result = dispatch(RS4);
            switch(RS4.ROBdest){
                case 1:
                    ROB1Result = dispatch(RS4);
                    ROB.ROB1.checkClock = current_clock;
                    break;
                case 2:
                    ROB2Result = dispatch(RS4);
                    ROB.ROB2.checkClock = current_clock;
                    break;
                case 3:
                    ROB3Result = dispatch(RS4);
                    ROB.ROB3.checkClock = current_clock;
                    break;
                case 4:
                    ROB4Result = dispatch(RS4);
                    ROB.ROB4.checkClock = current_clock;
                    break;
                case 5:
                    ROB5Result = dispatch(RS5);
                    ROB.ROB5.checkClock = current_clock;
                    break;
                default:
                    ROB6Result = dispatch(RS5);
                    ROB.ROB6.checkClock = current_clock;
                    break;
            }
            System.out.println("Dispatching RS4\n");
            //RS4.checkClock = current_clock;
        }
        if(RS5.getTag1() == 0 && RS5.getTag2() == 0 && RS5.checkClock != current_clock && RS5.isBusy()){
            //RS5Result = dispatch(RS5);
            switch(RS5.ROBdest){
                case 1:
                    ROB1Result = dispatch(RS5);
                    ROB.ROB1.checkClock = current_clock;
                    break;
                case 2:
                    ROB2Result = dispatch(RS5);
                    ROB.ROB2.checkClock = current_clock;
                    break;
                case 3:
                    ROB3Result = dispatch(RS5);
                    ROB.ROB3.checkClock = current_clock;
                    break;
                case 4:
                    ROB4Result = dispatch(RS5);
                    ROB.ROB4.checkClock = current_clock;
                    break;
                case 5:
                    ROB5Result = dispatch(RS5);
                    ROB.ROB5.checkClock = current_clock;
                    break;
                default:
                    ROB6Result = dispatch(RS5);
                    ROB.ROB6.checkClock = current_clock;
                    break;
            }
            System.out.println("Dispatching RS5\n");
        }
        
        String RS1op = "N/A", RS2op = "N/A", RS3op = "N/A", RS4op = "N/A", RS5op = "N/A", RS6op = "N/A";
        if(RS1.isBusy()){
            if(RS1.op == 0) RS1op = "Add"; if(RS1.op == 1) RS1op = "Sub"; if(RS1.op == 2) RS1op = "Mul"; if(RS1.op == 3) RS1op = "Div";}
        if(RS2.isBusy()){
            if(RS2.op == 0) RS2op = "Add"; if(RS2.op == 1) RS2op = "Sub"; if(RS2.op == 2) RS2op = "Mul"; if(RS2.op == 3) RS2op = "Div";}
        if(RS3.isBusy()){
            if(RS3.op == 0) RS3op = "Add"; if(RS3.op == 1) RS3op = "Sub"; if(RS3.op == 2) RS3op = "Mul"; if(RS3.op == 3) RS3op = "Div";}
        if(RS4.isBusy()){
            if(RS4.op == 0) RS4op = "Add"; if(RS4.op == 1) RS4op = "Sub"; if(RS4.op == 2) RS4op = "Mul"; if(RS4.op == 3) RS4op = "Div";}
        if(RS5.isBusy()){
            if(RS5.op == 0) RS5op = "Add"; if(RS5.op == 1) RS5op = "Sub"; if(RS5.op == 2) RS5op = "Mul"; if(RS5.op == 3) RS5op = "Div";}
        
        System.out.println("");
        
        System.out.println("            OP       Tag1    Tag2    Value1    Value2    ROBDest    Busy    ");
        System.out.println("");
        System.out.printf("RS1  %10s %7d %7d %7d %9d %8d %8d \n\n", RS1op, RS1.tag1, RS1.tag2, RS1.val1, RS1.val2, RS1.ROBdest, RS1.busy);
        System.out.printf("RS2  %10s %7d %7d %7d %9d %8d %8d \n\n", RS2op, RS2.tag1, RS2.tag2, RS2.val1, RS2.val2, RS2.ROBdest, RS2.busy);
        System.out.printf("RS3  %10s %7d %7d %7d %9d %8d %8d \n\n", RS3op, RS3.tag1, RS3.tag2, RS3.val1, RS3.val2, RS3.ROBdest, RS3.busy);
        System.out.printf("RS4  %10s %7d %7d %7d %9d %8d %8d \n\n", RS4op, RS4.tag1, RS4.tag2, RS4.val1, RS4.val2, RS4.ROBdest, RS4.busy);
        System.out.printf("RS5  %10s %7d %7d %7d %9d %8d %8d \n\n", RS5op, RS5.tag1, RS5.tag2, RS5.val1, RS5.val2, RS5.ROBdest, RS5.busy);


        System.out.println("    RF    RAT");
        System.out.printf("0:  %1d    %3d\n", Register_File[0], RAT[0]);
        System.out.printf("1:  %1d    %3d\n", Register_File[1], RAT[1]);
        System.out.printf("2:  %1d    %3d\n", Register_File[2], RAT[2]);
        System.out.printf("3:  %1d    %3d\n", Register_File[3], RAT[3]);
        System.out.printf("4:  %1d    %3d\n", Register_File[4], RAT[4]);
        System.out.printf("5:  %1d    %3d\n", Register_File[5], RAT[5]);
        System.out.printf("6:  %1d    %3d\n", Register_File[6], RAT[6]);
        System.out.printf("7:  %1d    %3d\n\n\n", Register_File[7], RAT[7]);   
        
        System.out.println("            Type     Dest     Val    Done    Exception    ");
        System.out.println("");
        System.out.printf("ROB1  %9s %7d %7d %7d %9d \n\n", ROB.ROB1.type, ROB.ROB1.dest, ROB.ROB1.val, ROB.ROB1.done, ROB.ROB1.exception);
        System.out.printf("ROB2  %9s %7d %7d %7d %9d \n\n", ROB.ROB2.type, ROB.ROB2.dest, ROB.ROB2.val, ROB.ROB2.done, ROB.ROB2.exception);
        System.out.printf("ROB3  %9s %7d %7d %7d %9d \n\n", ROB.ROB3.type, ROB.ROB3.dest, ROB.ROB3.val, ROB.ROB3.done, ROB.ROB3.exception);
        System.out.printf("ROB4  %9s %7d %7d %7d %9d \n\n", ROB.ROB4.type, ROB.ROB4.dest, ROB.ROB4.val, ROB.ROB4.done, ROB.ROB4.exception);
        System.out.printf("ROB5  %9s %7d %7d %7d %9d \n\n", ROB.ROB5.type, ROB.ROB5.dest, ROB.ROB5.val, ROB.ROB5.done, ROB.ROB5.exception);
        System.out.printf("ROB6  %9s %7d %7d %7d %9d \n\n\n", ROB.ROB6.type, ROB.ROB6.dest, ROB.ROB6.val, ROB.ROB6.done, ROB.ROB6.exception);

        if(ROB1Result != null && ROB.ROB1.checkClock + ROB.ROB1.compCost == current_clock){
            if(ROB.ROB1.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB1\n");
                for(int i = 0; i <= instCommitCount; i++)
                    temp_Queue.dequeue();
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB1, ROB1Result);
                System.out.println("Broadcasting ROB1\n");
                ROB.ROB1.checkClock = current_clock;
                ROB.ROB1.compCost = 0;
            }
            ROB1Result = null;
        }
        if(ROB2Result != null && ROB.ROB2.checkClock + ROB.ROB2.compCost == current_clock){
            if(ROB.ROB2.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB2\n");
                for(int i = 0; i <= instCommitCount; i++)
                    temp_Queue.dequeue();
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB2, ROB2Result);
                System.out.println("Broadcasting ROB2\n");
                ROB.ROB2.checkClock = current_clock;
                ROB.ROB2.compCost = 0;    
            }
            ROB2Result = null;
        }
        if(ROB3Result != null && ROB.ROB3.checkClock + ROB.ROB3.compCost == current_clock){
            if(ROB.ROB3.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB3\n");
                for(int i = 0; i <= instCommitCount; i++)
                    temp_Queue.dequeue();
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB3, ROB3Result);
                System.out.println("Broadcasting ROB3\n");
                ROB.ROB3.checkClock = current_clock;
                ROB.ROB3.compCost = 0;
            }
            ROB3Result = null;
        }
        if(ROB4Result != null && ROB.ROB4.checkClock + ROB.ROB4.compCost == current_clock){
            if(ROB.ROB4.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB4\n");
                for(int i = 0; i <= instCommitCount + 1; i++)
                    temp_Queue.dequeue();      
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB4, ROB4Result);
                System.out.println("Broadcasting ROB4\n");
                ROB.ROB4.checkClock = current_clock;
                ROB.ROB4.compCost = 0;
            }
            ROB4Result = null;
        }
        if(ROB5Result != null && ROB.ROB5.checkClock + ROB.ROB5.compCost + 1 == current_clock){
            if(ROB.ROB5.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB5\n");
                for(int i = 0; i <= instCommitCount + 1; i++)
                    temp_Queue.dequeue();      
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB5, ROB5Result);
                System.out.println("Broadcasting ROB5\n");
                ROB.ROB5.checkClock = current_clock;
                ROB.ROB5.compCost = 0;
            }
            ROB5Result = null;
        }
        if(ROB6Result != null && ROB.ROB6.checkClock + ROB.ROB6.compCost + 1 == current_clock){
            if(ROB.ROB6.exception == 1){
                ROB.flushROB();
                System.out.println("Flushing ROB because of ROB6\n");
                for(int i = 0; i <= instCommitCount + 1; i++)
                    temp_Queue.dequeue();      
                inst_Queue = temp_Queue;
                instCommitCount = 0;
            }
            else{
                broadcast(ROB.ROB6, ROB6Result);
                System.out.println("Broadcasting ROB6\n");
                ROB.ROB6.checkClock = current_clock;
                ROB.ROB6.compCost = 0;
            }
            ROB6Result = null;
        }
        
        System.out.println("");
        
        while(true){
            if(ROB.ROB1.done == 1 && ROB.ROB1.type != 4 && ROB.ROB1.checkClock != current_clock && ROB.ROB1.instNum == lowestROBNum){               
                commit(ROB.ROB1);
                System.out.println("Committing ROB1\n");
                instCommitCount++;
                ROB.ROB1.type = 4;
                break;
            }
            if(ROB.ROB2.done == 1 && ROB.ROB2.type != 4 && ROB.ROB2.checkClock != current_clock && ROB.ROB2.instNum == lowestROBNum){
                commit(ROB.ROB2);
                System.out.println("Committing ROB2\n");
                instCommitCount++;
                ROB.ROB2.type = 4;
                break;
            }
            if(ROB.ROB3.done == 1 && ROB.ROB3.type != 4 && ROB.ROB3.checkClock != current_clock && ROB.ROB3.instNum == lowestROBNum){
                commit(ROB.ROB3);
                System.out.println("Committing ROB3\n");
                instCommitCount++;
                ROB.ROB3.type = 4;
                break;
            }
            if(ROB.ROB4.done == 1 && ROB.ROB4.type != 4 && ROB.ROB4.checkClock != current_clock && ROB.ROB4.instNum == lowestROBNum){
                commit(ROB.ROB4);
                System.out.println("Committing ROB4\n");
                instCommitCount++;
                ROB.ROB4.type = 4;
                break;
            }
            if(ROB.ROB5.done == 1 && ROB.ROB5.type != 4 && ROB.ROB5.checkClock != current_clock && ROB.ROB5.instNum == lowestROBNum){
                commit(ROB.ROB5);
                System.out.println("Committing ROB5\n");
                instCommitCount++;
                ROB.ROB5.type = 4;
                break;
            }
            if(ROB.ROB6.done == 1 && ROB.ROB6.type != 4 && ROB.ROB6.checkClock != current_clock && ROB.ROB6.instNum == lowestROBNum){
                commit(ROB.ROB6);
                System.out.println("Committing ROB6\n");
                instCommitCount++;
                ROB.ROB6.type = 4;
                break;
            }
            lowestROBNum = ROB.ROB1.instNum;
            if(lowestROBNum > ROB.ROB2.instNum && ROB.ROB2.instNum != 0 || lowestROBNum == 0){
                lowestROBNum = ROB.ROB2.instNum;
            }
            if(lowestROBNum > ROB.ROB3.instNum && ROB.ROB3.instNum != 0 || lowestROBNum == 0){
                lowestROBNum = ROB.ROB3.instNum;
            }
            if(lowestROBNum > ROB.ROB4.instNum && ROB.ROB4.instNum != 0 || lowestROBNum == 0){
                lowestROBNum = ROB.ROB4.instNum;
            }
            if(lowestROBNum > ROB.ROB5.instNum && ROB.ROB5.instNum != 0 || lowestROBNum == 0){
                lowestROBNum = ROB.ROB5.instNum;
            }
            if(lowestROBNum > ROB.ROB6.instNum && ROB.ROB6.instNum != 0 || lowestROBNum == 0){
                lowestROBNum = ROB.ROB6.instNum;
            }
            break;
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * This is the main function for the file that will extract the information
     * from the file and represent the instructions accordingly
     */
    public static void main(String[] args) {
        
        System.out.println("Please input the file directory: ");
        Scanner scan = new Scanner(System.in); //Used to read the file
        String dir = scan.nextLine();
        File dir2 = new File(dir);
        File[] files = dir2.listFiles();
  
        for (File f : files )
        {
            try{
                Scanner input = new Scanner(f);

                //This loop will go through the file to get the number of instructions
                //and the number of cycles
                for(int i = 0; i<=1; i++){
                    if(i == 0)
                        string_num_of_instrs = input.nextLine();
                    if(i == 1)
                        num_cycles = input.nextLine();
                }
                sim_clock = Integer.parseInt(num_cycles);
                int_num_of_instrs = Integer.parseInt(string_num_of_instrs);

                //This loop will go through the file to grab/ decode the instructions and
                //load them into the queue
                for(int i = 0; i < int_num_of_instrs; i++){
                    String instr = input.nextLine(); //Read the next line of the file
                    decoded_instr = instruction_decode(instr); //Decode the instruction
                    inst_Queue.enqueue(decoded_instr); //Put the instruction in the queue
                    temp_Queue.enqueue(decoded_instr); //Instructions preserved to deal with exception
                    System.out.println(decoded_instr);
                }

                //This loop will go through the file and assign register values
                for(int i = 0; i <= 7; i++){
                    String value = input.nextLine();
                    updateRF(i, Integer.parseInt(value));
                    System.out.printf("Register %d: %d\n", i, Register_File[i]);
                }
                System.out.println("");
            }
            catch(FileNotFoundException e)
            {                
            }
        for(int i = 0; i < sim_clock; i++){
            current_clock++;
            System.out.printf("//////////////////////Cycle %d/////////////////////////\n", current_clock);
            TOM();
            System.out.printf("//////////////////////Cycle %d/////////////////////////\n", current_clock);
        }
        System.out.println("");
        String RS1op = "N/A", RS2op = "N/A", RS3op = "N/A", RS4op = "N/A", RS5op = "N/A";
        if(RS1.isBusy()){
            if(RS1.op == 0) RS1op = "Add"; if(RS1.op == 1) RS1op = "Sub"; if(RS1.op == 2) RS1op = "Mul"; if(RS1.op == 3) RS1op = "Div";}
        if(RS2.isBusy()){
            if(RS2.op == 0) RS2op = "Add"; if(RS2.op == 1) RS2op = "Sub"; if(RS2.op == 2) RS2op = "Mul"; if(RS2.op == 3) RS2op = "Div";}
        if(RS3.isBusy()){
            if(RS3.op == 0) RS3op = "Add"; if(RS3.op == 1) RS3op = "Sub"; if(RS3.op == 2) RS3op = "Mul"; if(RS3.op == 3) RS3op = "Div";}
        if(RS4.isBusy()){
            if(RS4.op == 0) RS4op = "Add"; if(RS4.op == 1) RS4op = "Sub"; if(RS4.op == 2) RS4op = "Mul"; if(RS4.op == 3) RS4op = "Div";}
        if(RS5.isBusy()){
            if(RS5.op == 0) RS5op = "Add"; if(RS5.op == 1) RS5op = "Sub"; if(RS5.op == 2) RS5op = "Mul"; if(RS5.op == 3) RS5op = "Div";}
          
            
        System.out.printf("Final result after %d cycles: \n\n", sim_clock);
        System.out.println("            OP       Tag1    Tag2    Value1    Value2    ROBDest    Busy    ");
        System.out.println("");
        System.out.printf("RS1  %9s %7d %7d %9d %9d %8d %9d \n\n", RS1op, RS1.tag1, RS1.tag2, RS1.val1, RS1.val2, RS1.ROBdest, RS1.busy);
        System.out.printf("RS2  %9s %7d %7d %9d %9d %8d %9d \n\n", RS2op, RS2.tag1, RS2.tag2, RS2.val1, RS2.val2, RS2.ROBdest, RS2.busy);
        System.out.printf("RS3  %9s %7d %7d %9d %9d %8d %9d \n\n", RS3op, RS3.tag1, RS3.tag2, RS3.val1, RS3.val2, RS3.ROBdest, RS3.busy);
        System.out.printf("RS4  %9s %7d %7d %9d %9d %8d %9d \n\n", RS4op, RS4.tag1, RS4.tag2, RS4.val1, RS4.val2, RS4.ROBdest, RS4.busy);
        System.out.printf("RS5  %9s %7d %7d %9d %9d %8d %9d \n\n", RS5op, RS5.tag1, RS5.tag2, RS5.val1, RS5.val2, RS5.ROBdest, RS5.busy); 
            
        System.out.println("");
        System.out.println("");
        
        System.out.println("            Type     Dest     Val    Done    Exception    ");
        System.out.println("");
        System.out.printf("ROB1  %9s %7d %7d %7d %9d \n\n", ROB.ROB1.type, ROB.ROB1.dest, ROB.ROB1.val, ROB.ROB1.done, ROB.ROB1.exception);
        System.out.printf("ROB2  %9s %7d %7d %7d %9d \n\n", ROB.ROB2.type, ROB.ROB2.dest, ROB.ROB2.val, ROB.ROB2.done, ROB.ROB2.exception);
        System.out.printf("ROB3  %9s %7d %7d %7d %9d \n\n", ROB.ROB3.type, ROB.ROB3.dest, ROB.ROB3.val, ROB.ROB3.done, ROB.ROB3.exception);
        System.out.printf("ROB4  %9s %7d %7d %7d %9d \n\n", ROB.ROB4.type, ROB.ROB4.dest, ROB.ROB4.val, ROB.ROB4.done, ROB.ROB4.exception);
        System.out.printf("ROB5  %9s %7d %7d %7d %9d \n\n", ROB.ROB5.type, ROB.ROB5.dest, ROB.ROB5.val, ROB.ROB5.done, ROB.ROB5.exception);
        System.out.printf("ROB6  %9s %7d %7d %7d %9d \n\n\n", ROB.ROB6.type, ROB.ROB6.dest, ROB.ROB6.val, ROB.ROB6.done, ROB.ROB6.exception);
        
        System.out.println("    RF    RAT");
        System.out.printf("0:  %1d    %3d\n", Register_File[0], RAT[0]);
        System.out.printf("1:  %1d    %3d\n", Register_File[1], RAT[1]);
        System.out.printf("2:  %1d    %3d\n", Register_File[2], RAT[2]);
        System.out.printf("3:  %1d    %3d\n", Register_File[3], RAT[3]);
        System.out.printf("4:  %1d    %3d\n", Register_File[4], RAT[4]);
        System.out.printf("5:  %1d    %3d\n", Register_File[5], RAT[5]);
        System.out.printf("6:  %1d    %3d\n", Register_File[6], RAT[6]);
        System.out.printf("7:  %1d    %3d\n\n\n\n", Register_File[7], RAT[7]);
        }
        
        System.out.println("Instruction Queue: \n");
        while(!inst_Queue.isEmpty()){
            String temp_instr = inst_Queue.dequeue();
            System.out.println(temp_instr);
        }
    } 
}