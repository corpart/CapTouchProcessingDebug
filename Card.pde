class Card {
  static final int TOUCHSTART = 10 ; 
  static final int TOUCHING = 11 ;
  static final int TOUCHEND  = 12; 
  static final int   NIL = -1 ; 

    int bufferSize = 5;  // size of averaging window 10 
    int bufferIndex = 0 ;  
   float[] buffer = new float[bufferSize];  //for calculating the average of recent numbers 
     
    float hoverThreshold = 0.2; 
    float touchThreshold = 0.6; 
    
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
    color fillColor; 
   
    color[] colors = {
      
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
    
     
    
    void init(int ind){
      index = ind ; 
       fillColor = colors[ind%15] ;
       caliScale = 1.0; 
       caliOffset = 0 ; 
       caliPeak = 0 ; 
       caliIndex = 0; 
       
        for (int i = 0 ; i < bufferSize;  i++ ){
             buffer[i] = 0 ;
        }


          isTouching = false ; 
        wasTouching = false ; 


    }
    
 
    void update( ) {
      //how far finger is from card 
        hoverScale = calHoverScale() ;
        // different stages of touch 
        touchEvent = updateTouchEvent(); 
     }
     
     float calHoverScale () {
// (average-hoverThreshold)/(touchThreshold-hoverThreshold) ;     
      float f =  (average-caliOffset)*caliScale; 
      return (f-hoverThreshold) / (touchThreshold-hoverThreshold) ;  
      // return (average-caliOffset)  ; 
        
    }
    
    //----DATA METHODS---- 
    void addNewData(float f){
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

    void startOffsetCalibration(){
      caliIndex =  0 ;
      isCalibratingOffset = true;   
      //caliOffset = 0 ; 
    }

    void startPeakCalibration () { 
      isCalibratingPeak = true; 
    }

    void stopPeakCalibration () { 
      isCalibratingPeak = false; 
      println("card " + index + "| peak" + caliPeak + "| offset" + caliOffset ) ;
      if (caliPeak > caliOffset){
        caliScale = 1.0/(caliPeak-caliOffset); 
        xmlCards[index].setFloat("scale", caliScale);
        saveXML(xml, "data/cards.xml");

      }else {
        println("ERROR Run peak calibration first by pressing P");
      }
    }

    float calAverage(float[] a){
        float ave = 0 ; 
        for (int i = 0 ; i < a.length;  i++ ){
             ave += a[i];
        }
      return ave/a.length;
    }
    

      int updateTouchEvent(){
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