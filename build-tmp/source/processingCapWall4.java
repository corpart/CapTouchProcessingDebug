import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class processingCapWall4 extends PApplet {

/*
works with arduinoCapWall4 to plot data 
 */


//// plotting 
float xPos  = 0 ; 
float plotWindowY = 300 ;
float plotWidth ; 

//cards 
// final static int TOTOAL_CARDS = 15;  
int TOTOAL_CARDS; 
Card[] cards; 
float offsetX = 0;
float offsetY = 0; 
float yScale = 1; 

float[] wallValues;
Serial myPort;

//these are the ones to be plotted and printed 
//int[] channels = {0,1,2};
//int[] channels = {3,4,5};
//int[] channels = {6,7,8};
//int[] channels = {9,10,11,12,13,14};
int[] channels = {53};
int[] allChannels; 

//data
XML xml; 
XML[] xmlCards ; 
JSONArray json;  
int jsonInd; 

public void setup() {

  size (1000, 400);
  background(255); 
  imageMode(CENTER);
  plotWidth = width; 
  textSize(26);

  yScale= 100; 
  offsetX  = 970 ; //width/2; 
  offsetY  = height / 2 ; 
  // List all the available serial ports
  // if using Processing 2.1 or later, use Serial.printArray()
  println(Serial.list());

  // I know that the first port in the serial list on my mac
  // is always my  Arduino, so I open Serial.list()[0].
  // Open whatever port is the one you're using.
  myPort = new Serial(this, Serial.list()[2], 9600);

  // don't generate a serialEvent() unless you get a newline character:
  myPort.bufferUntil('\n');

  // card meta data is stored in this file 
  xml = loadXML("cards.xml");
  xmlCards = xml.getChildren("card");
  //decide how many cards there are in total 
  TOTOAL_CARDS = xmlCards.length; 
  println("TOTOAL_CARDS: "+ TOTOAL_CARDS);
  // this is to one line of data from processing 
  allChannels = new int[TOTOAL_CARDS];
  wallValues = new float[TOTOAL_CARDS]; //potentially hold more values 
  
  //initializing card objects 
  cards = new Card[TOTOAL_CARDS];
  for (int i = 0; i < TOTOAL_CARDS; i++ ) {
    allChannels[i] = i;
    cards[i] = new Card (i);
    cards[i].caliOffset = xmlCards[i].getFloat("offset");
    cards[i].caliScale = xmlCards[i].getFloat("scale");
  
  }
  
  // taking care of saving json file 
  prepareExitHandler(); //so that exit function is called 
  json = new JSONArray();
  jsonInd = 0 ; 

}

public void draw() {
  
  // check on all the cards 
  for (int i = 0; i < TOTOAL_CARDS; i++ ) {
    //TODO  shall this be called only at new Serial event to be faster ? 
    cards[i].update();
    
    if (cards[i].touchEvent == 10){
      //start touch event 
      JSONObject temp = new JSONObject();
      int m = millis(); 
      temp.setInt("card", i );
      temp.setString("stamp", "start" );
      temp.setInt("time", m);
      json.setJSONObject(jsonInd, temp);
      jsonInd++; 
      println("i: "+i + " start: " + m);
    }else if(cards[i].touchEvent == 12){
      //end touch event 
      JSONObject temp = new JSONObject();
      int m = millis(); 
      temp.setInt("card", i );
      temp.setString("stamp", "end" );
      temp.setInt("time", m);
      json.setJSONObject(jsonInd, temp);
      jsonInd++; 
      
      println("i: "+i + " end: " + m);
    }
  }
  //  fill(255);
 // rect(0, height-40, width, height-10);
 
  //plotSerialData();
  // plotSerialData(channels); 
  plotSerialData(allChannels);
  
  
}




public void serialEvent(Serial myPort) {

  // get the ASCII string:
  String inString = myPort.readStringUntil('\n');

  if (inString != null) {
    // trim off any whitespace:
    inString = trim(inString);

    // split the string on the commas and convert the
    // resulting substrings into an integer array:
    float[] values = PApplet.parseFloat(split(inString, "\t"));

    if (values.length == TOTOAL_CARDS) { //just gonna ignore the errors s
      //source -> dest 
      arrayCopy(values, wallValues);

     for (int i = 0; i < TOTOAL_CARDS; i++ ) {
    
        cards[i].addNewData(wallValues[i]) ;
        
      }
    
    }else{
      // String s = "Mismatch:  TOTOAL_CARDS" + TOTOAL_CARDS 
      //   + "array size " + values.length ; 
        
      // println(s); 
    }
  }
}


public void plotSerialData() {
  noStroke(); 
  for (int i = 0; i < TOTOAL_CARDS; i++ ) {
    fill(cards[i].fillColor);
    ellipse ( xPos, -cards[i].hoverScale*yScale + plotWindowY - 20, 1, 1);

    // strokeWeight(2);
    // line( 0, plotWindowY - 10 - cards[i].caliOffset, width , plotWindowY - 10 - cards[i].caliOffset) ;
  }


  if (xPos >= plotWidth) {
    xPos = 0 ;
     repaint(); 
  } else {
    xPos ++ ;
  }


}

public void repaint(){
   fill(255); 
    // rect(0, 0, plotWidth, plotWindowY   );  //repaint the graph area

    rect(0, 0, plotWidth, height   );  //repaint the graph area
   
}


public void saveToJson() {
// } void keyPressed() {
  String s = "data/"; 
  s += String.valueOf(year());
  s += String.valueOf(month()); 
  s += String.valueOf(day());
  s += String.valueOf(hour());  
  s += String.valueOf(minute()); 
  s += ".json"; 
  saveJSONArray(json, s);
  println("save to "+s); 
}


private void prepareExitHandler () {
  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run () {
      // System.out.println("SHUTDOWN HOOK");
      // application exit code here
      saveToJson(); 
    }
  }));
}



public void plotSerialData(int[] array) {
  noStroke(); 
  for (int i = 0; i < array.length; i++ ) {
    int k = array[i];  
    // plotReadings 
    fill(cards[k].fillColor) ; 
    ellipse ( xPos, -cards[k].hoverScale * yScale  + plotWindowY - 10, 2, 2);  
  //  print(k +" " + -cards[k].hoverScale +"\t " );
    
  }
  //println(); 
  if (xPos >= plotWidth) {
    xPos = 0 ;
   repaint(); 
  } else {
    xPos ++ ;
  }
}
class Card {
  static final int TOUCHSTART = 10 ; 
  static final int TOUCHING = 11 ;
  static final int TOUCHEND  = 12; 
  static final int   NIL = -1 ; 

    int bufferSize = 5;  // size of averaging window 10 
    int bufferIndex = 0 ;  
   float[] buffer = new float[bufferSize];  //for calculating the average of recent numbers 
     
    float hoverThreshold = 0.2f; 
    float touchThreshold = 0.6f; 
    
    boolean isTouching  ;
    boolean wasTouching ; 


    int caliBufferSize = 200 ;  //average these to cancel ground noise 
    float[] caliBuffer = new float[caliBufferSize] ; 
    float average = 0 ;   

    boolean isCalibratingOffset = false; //flag for calibrating base line noise
    boolean isCalibratingPeak = false; //flag for calibrating peak value 
    int caliIndex = 0 ;  //ind for calibrating offset 
    
    float caliOffset; //baseline 
    float caliPeak ; //max value 
    float caliScale; //scaling to normalize 
    
    float hoverScale = 1; 
    
     int touchEvent ; 

//which card it is on the board (0-29)
    int index ; 
    int fillColor; 
   
    int[] colors = {
      
      color(0,100,0),
      color(100,100,0),
      color(100,0,100),
      
      color(100,0,0),
      color(0,0,100),
      color(0,200,50),
      
      color(100),
      color(50,255,100),
       color(0,100,200),
       
      color(100,100,200),
      color(200,0,100),
      color(250,50,200),
      color(0,0,200),
      
      color(0,200,100),
      color(200,100,100),

      color(0,100,0),
      color(100,100,0),
      color(100,0,100),
      
      color(100,0,0),
      color(0,0,100),
      color(0,200,50),
      
      color(100),
      color(50,255,100),
       color(0,100,200),
       
      color(100,100,200),
      color(200,0,100),
      color(250,50,200),
      color(0,0,200),
      
      color(0,200,100),
      color(200,100,100)
      
      
    } ; 


   
    Card(int i){
        init(i) ;
    }
    
     
    
    public void init(int ind){
      index = ind ; 
       fillColor = colors[ind%15] ;
       caliScale = 1.0f; 
       caliOffset = 0 ; 
       caliPeak = 0 ; 
       caliIndex = 0; 
       
        for (int i = 0 ; i < bufferSize;  i++ ){
             buffer[i] = 0 ;
        }


          isTouching = false ; 
        wasTouching = false ; 


    }
    
 
    public void update( ) {
      //how far finger is from card 
        hoverScale = calHoverScale() ;
        // different stages of touch 
        touchEvent = updateTouchEvent(); 
     }
     
     public float calHoverScale () {
// (average-hoverThreshold)/(touchThreshold-hoverThreshold) ;     
      float f =  (average-caliOffset)*caliScale; 
      return (f-hoverThreshold) / (touchThreshold-hoverThreshold) ;  
      // return (average-caliOffset)  ; 
        
    }
    
    //----DATA METHODS---- 
    public void addNewData(float f){
      if (isCalibratingOffset){
          caliBuffer[caliIndex] = f; 
          caliIndex++; 
         // caliOffset = calAverage (caliBuffer) ; 
          if(caliIndex >= caliBufferSize){
            isCalibratingOffset = false ; 
            caliOffset = calAverage (caliBuffer) ; 
            //change the number stored 
            xmlCards[index].setFloat("offset", caliOffset);
            saveXML(xml, "data/cards.xml");

            caliIndex =  0 ; 

            if (index == 0 ){ //just so that we dont print multiple times 
              println("Finished calibraing offset");

            }
         }
        return ; 
       }

       if (isCalibratingPeak){
        if(f > caliPeak){
          caliPeak = f ; 

        }
        // return ; 
       }


      buffer[bufferIndex] = f ; 

      bufferIndex++; 
       if(bufferIndex >= bufferSize){
          bufferIndex =  0 ;
       }
       average = calAverage (buffer);
       
       
    }

    public void startOffsetCalibration(){
      caliIndex =  0 ;
      isCalibratingOffset = true;   
      //caliOffset = 0 ; 
    }

    public void startPeakCalibration () { 
      isCalibratingPeak = true; 
    }

    public void stopPeakCalibration () { 
      isCalibratingPeak = false; 
      println("card " + index + "| peak" + caliPeak + "| offset" + caliOffset ) ;
      if (caliPeak > caliOffset){
        caliScale = 1.0f/(caliPeak-caliOffset); 
        xmlCards[index].setFloat("scale", caliScale);
        saveXML(xml, "data/cards.xml");

      }else {
        println("ERROR Run peak calibration first by pressing P");
      }
    }

    public float calAverage(float[] a){
        float ave = 0 ; 
        for (int i = 0 ; i < a.length;  i++ ){
             ave += a[i];
        }
      return ave/a.length;
    }
    

      public int updateTouchEvent(){
        if(hoverScale>= 1){
                if (!isTouching) { //raising edge 
                     isTouching = true; 
                     // println("touchstart: "+ index);
                     return TOUCHSTART ; 

                }
                isTouching = true; 
                return TOUCHING; 
        }else if (hoverScale <= 0 ){ 
                if(isTouching) { //dropping edge 
                    wasTouching = true ;
                   isTouching = false ; 
                   return TOUCHEND; 
                }
                isTouching = false ; 
                
        }
        
        return NIL ; 

    }
}
public void keyPressed() {
  if (key == 'b' || key == 'B') {
    println("Start calibraing offset. This will automatically stop.");
    for (int i = 0; i < TOTOAL_CARDS; i++ ) {
        cards[i].startOffsetCalibration();
      }
  } 
  if (key == 'p' || key == 'P') {
    if (cards.length >0 ){
        //start 
        if (!cards[0].isCalibratingPeak){
            for (int i = 0; i < TOTOAL_CARDS; i++ ) {
                cards[i].startPeakCalibration(); 
            }
            println("Start calibraing Peak. Touch each card for 1S. Press P when done. ");
        } else {  //if ending 
             for (int i = 0; i < TOTOAL_CARDS; i++ ) {
                cards[i].stopPeakCalibration(); 
            }
        }

    }
  } 


}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "processingCapWall4" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
