void keyPressed() {
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