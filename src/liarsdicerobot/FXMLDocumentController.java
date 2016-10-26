/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package liarsdicerobot;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/**
 *
 * @author cstout
 */
public class FXMLDocumentController implements Initializable {
    ObservableList<String> oneThroughSix = FXCollections.observableArrayList("0","1", "2", "3", "4", "5", "6");
    ObservableList<String> oneThroughTwelve = FXCollections.observableArrayList("0","1", "2", "3", "4", "5", "6","7","8","9","10","11","12");
    final double smallChance = .166666666666666666666;
    final double biggerChance = .8333333333333333333;
    double[][] probabilities = new double[18][7];
    double[][] probabilitiesWithInput = new double[18][7];
    int totalDice = 10;
    int amountOfMyDice = 5;
    int[] knownDice = {0,0,0,0,0,0};
    double[] otherGuesses = {0,0,0,0,0,0};
    int lastTurnAmount = 0;
    int lastTurnValue = 0;
    int turn = 0;
    boolean bluffValue = false;
        
    @FXML
    private ImageView Image1;
    
    @FXML
    private Label output;
    
    @FXML
    private ChoiceBox opponentAmount;

    @FXML
    private ChoiceBox opponentValue;
    @FXML
    private TextField totalDiceLabel;

    
    
   
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       rollForANewRound();
       opponentValue.setItems(oneThroughSix);
       opponentAmount.setItems(oneThroughTwelve);     
       
    }  
    
    @FXML
    private void TakeTurn(ActionEvent action){
        turn++;
        bluffValue = bluff();
        if (turn == 1){
           int myTurnValue = 0;
           int myTurnAmount = (totalDice/5);
                if (bluffValue == true){ 
                    if(knownDice[5]<2){ 
                        myTurnValue = 6;
                    }else if(knownDice[5]<1){
                        myTurnValue = 5;
                    }else{
                        myTurnValue = 4;
                        
                    }
                }else{
                    myTurnValue = maxDiceNormalArray(knownDice);   
                }  
                output.setText(myTurnAmount+" "+myTurnValue+"s");
                output.setWrapText(true);
     
       } else{
        findBestMove();
            
       }
    }
    private void oneOnOne(){
        turn++;
        int max = maxDiceNormalArray(knownDice);
        if (totalDice==2){
            if (turn ==1){
                
                 if (max<4){
                    output.setText("1 6s");
                    output.setWrapText(true);
                 
                }else{
                    output.setText("1 "+ max+("s"));
                    output.setWrapText(true);
                
                }
            } else{
               if(max>lastTurnValue){
                   output.setText("1 "+ max+("s"));
                   output.setWrapText(true);
                   
               }else if(max<lastTurnValue){
                   output.setText("1 "+ max+("s"));
                   output.setWrapText(true);
               }
                
            }
            
        }else{
            
            
        }
        
        
    }
    
    void findBestMove() {
       clearTheArrays();
       totalDice = Integer.parseInt(totalDiceLabel.getText());
       fillArray(lastTurnAmount, lastTurnValue);
       printArray(probabilities);
       double[] maxProbability = findMax(probabilities);
       
       
       
       if(maxProbability[2] < .30){
           
           //output.setFont(Font("Cambria", 32));
           String myCurrentDice = "";
           for(int i = 0; i<knownDice.length; i++){
                myCurrentDice += (knownDice[i] + " " + (i+1)+ "s\n\r");
           }
           output.setText("Call that Bullshit\n\r"+myCurrentDice);
           output.setWrapText(true); 
           
       } else if(maxProbability[2]<.49){
           fillArrayWithInputs(lastTurnAmount, lastTurnValue);
           double[] maxWithInputs = findMax(probabilitiesWithInput);
           if (maxWithInputs[2] > .50){
               output.setText(maxWithInputs[0] + " " + maxWithInputs[1]+ "s this is the one that takes user input");
               output.setWrapText(true);
               System.out.println("IT messed up in spot 1");
           }else{
               
               
               String myCurrentDice = "";
               for(int i = 0; i<knownDice.length; i++){
                  myCurrentDice += (knownDice[i] + " " + (i+1)+ "s\n\r");
               }
               output.setText("Its close, but call that Bullshit\n\r"+myCurrentDice);
               output.setWrapText(true);
           }
           
       }else{
           if(bluffValue == true){
               maxProbability[1] = minDiceNormalArray(knownDice); 
               
           }
           output.setText("Amount: "+ (int)maxProbability[0]+" "+"Value: "+(int)maxProbability[1]);
           output.setWrapText(true);
           System.out.println("it messed up in spot 2");
       
       }
    }
    
    @FXML
    void makeOpponentMove(ActionEvent event) {
        int val = (Integer.parseInt((opponentValue.getValue()).toString()));
        int am = (Integer.parseInt((opponentAmount.getValue()).toString()));
        otherGuesses[val-1] = (otherGuesses[val-1]+.33);
        lastTurnAmount = am;
        lastTurnValue = val;
        
        System.out.println("the last turn amount = "+am);
        System.out.println("the last turn value = "+val);
        turn++;
        
    }
    @FXML
    void nextRoundWon(ActionEvent event) {
        totalDice = Integer.parseInt(totalDiceLabel.getText());
        totalDice--;
        totalDiceLabel.setText(Integer.toString(totalDice)); 
        for (int i =0; i<otherGuesses.length;i++){
            otherGuesses[i] = 0;
            knownDice[i] = 0;
        }
        lastTurnAmount = 0;
        lastTurnValue = 0;
        
        output.setText("");
        opponentValue.setValue("0");
        opponentAmount.setValue("0");
        
        for (int i =0; i<otherGuesses.length;i++){
            System.out.println(otherGuesses[i]);
            System.out.print(" ");
            System.out.print(knownDice[i] = 0);
        }
        turn = 0;
        rollForANewRound();
     
        
    }
     @FXML
    void nextRoundLost(ActionEvent event) {
        totalDice = Integer.parseInt(totalDiceLabel.getText());
        totalDice--;
        totalDiceLabel.setText(Integer.toString(totalDice)); 
        for (int i =0; i<otherGuesses.length;i++){
            otherGuesses[i] = 0;
            knownDice[i] = 0;
        }
        lastTurnAmount = 0;
        lastTurnValue = 0;
        
        output.setText("");
        opponentValue.setValue("0");
        opponentAmount.setValue("0");
        
        for (int i =0; i<otherGuesses.length;i++){
            System.out.println(otherGuesses[i]);
            System.out.print(" ");
            System.out.print(knownDice[i] = 0);
        }
        amountOfMyDice--;
        rollForANewRound();
        turn = 0;
    }
    
   
    @FXML
    void changeTotalDice(ActionEvent event) {
        totalDice = Integer.parseInt(totalDiceLabel.getText());
        System.out.println(totalDice);
    }

    
    public void fillArray(int lastTurnAmount, int lastTurnValue){
        int removeMove = 0;
        for(int i = lastTurnAmount; i<lastTurnAmount+5; i++){
            if(lastTurnAmount == i){
                for (int j = lastTurnValue; j<7; j++){
                    
                    if(removeMove == 0){
                        probabilities[i][j] = 0;
                        removeMove++;
                        
                    }else{                        
                        double result = getProbability(i, j);
                        probabilities[i][j] = result;
                    }
                    
                }
                
            }else{
                for (int k = 1; k<7; k++){
                    double result = getProbability(i, k);
                    probabilities[i][k] = result;
                }
                
            }
            
        }
        
    }
    
    private double getProbability(int amount, int value) {
        int totalOtherVisableDice = totalArrayWithoutOneValue(knownDice, value);
        System.out.println(totalOtherVisableDice);
        double probability = 1;
        
        int myDiceOfThisNumber = knownDice[value-1];
        double expected = amount-myDiceOfThisNumber;
        double totalCalculatedDice = totalDice-(myDiceOfThisNumber+totalOtherVisableDice);
       
        for (int i = 0; i<expected;i++){
            double partOne = getFactorialShit(i, totalCalculatedDice); //2, 13       
            double partTwo = Math.pow(smallChance,i);       
            double partThree = Math.pow(biggerChance,(totalCalculatedDice-i));               
            probability = probability- (partOne*partTwo*partThree);
       }
       return probability;
    }
    private double getFactorialShit(double expectedNumber, double thisTotalDice){
        double i = thisTotalDice-expectedNumber;  
        double total = 1;
        for(double temp = thisTotalDice; temp>i; temp--){
            total = total*temp;
        }
        double secondTotal = 1;
        for(double other = expectedNumber; other>1; other--){
            
            secondTotal = secondTotal*other;
        }
        total = total/secondTotal;
        return total;
    }
    public static void printArray(double matrix[][]) {
        for (double[] row : matrix) 
            System.out.println(Arrays.toString(row));       
    } 
    public double[] findMax(double matrix[][]) {
        double[] sendInfo = new double[3];
        double maxValue = 0;
        int amount = 0;
        int value =0;
        for(int i = 1; i<matrix.length; i++){
            for(int j = 1; j<matrix[0].length; j++){
                if(matrix[i][j]>=maxValue){
                    maxValue = matrix[i][j];
                    amount = i;
                    value = j;
                    
                }
                
            }
            
        }
        System.out.println("Max Value is: "+amount +" of the value "+ value+" with a probability of " + maxValue);
        sendInfo[0] = amount;
        sendInfo[1] = value;
        sendInfo[2] = maxValue;
        return sendInfo;
    } 
    public static int totalArrayWithoutOneValue(int[] array, int value){
        int total = 0;
        for (int i = 0;i<array.length;i++){
            if((i+1)!=value){
                total = total+array[i];
            }
            
        }
        return total;
        
    }
    public static double totalArrayWithoutOneValue(double[] array, int value){
        double total = 0;
        for (int i = 0;i<array.length;i++){
            if((i+1)!=value){
                total = total+array[i];
            }
            
        }
        return total;
        
    }
  
    private double getProbabilityWithOutsideInput(int amount, int value) {
        double[] arrayWithInput = addArrays(knownDice, otherGuesses);
        double totalOtherVisableDice = totalArrayWithoutOneValue(arrayWithInput, value);
        System.out.println(totalOtherVisableDice);
        double probability = 1;
        
        double myDiceOfThisNumber = arrayWithInput[value-1];
        double expected = amount-myDiceOfThisNumber;
        double totalCalculatedDice = totalDice-(myDiceOfThisNumber+totalOtherVisableDice);
       
        for (int i = 0; i<expected;i++){
            double partOne = getFactorialShit(i, totalCalculatedDice); //2, 13       
            double partTwo = Math.pow(smallChance,i);       
            double partThree = Math.pow(biggerChance,(totalCalculatedDice-i));               
            probability = probability- (partOne*partTwo*partThree);
       }
       return probability;
    }
    public void fillArrayWithInputs(int lastTurnAmount, int lastTurnValue){
        int removeMove = 0;
        for(int i = lastTurnAmount; i<lastTurnAmount+5; i++){
            if(lastTurnAmount == i){
                for (int j = lastTurnValue; j<7; j++){
                    
                    if(removeMove == 0){
                        probabilitiesWithInput[i][j] = 0;
                        removeMove++;
                        
                    }else{                        
                        double result = getProbabilityWithOutsideInput(i, j);
                        probabilitiesWithInput[i][j] = result;
                    }
                    
                }
                
            }else{
                for (int k = 1; k<7; k++){
                    double result = getProbabilityWithOutsideInput(i, k);
                    probabilitiesWithInput[i][k] = result;
                }
                
            }
            
        }
        
    }
    
    private double[] addArrays(int[] a, double[] b){
        double[] c = new double[6];
        for(int i = 0; i<a.length;i++){
            c[i] = (a[i]+b[i]);
            
        }
        
        
        return c;
    }
    private void clearTheArrays(){
        for (int i = 0;i<probabilities.length; i++){
            for(int j = 0; j<probabilities[0].length; j++){
                probabilities[i][j] = 0;
                probabilitiesWithInput[i][j] = 0;
                
            }
            
        }
        
    }
    private void rollForANewRound(){
        Random rand = new Random();
        for (int i = 0; i<knownDice.length; i++){
            knownDice[i] = 0;
            
        }
        for(int i = 0; i<amountOfMyDice;i++){
            
            int temp = rand.nextInt(6);
            knownDice[temp]++;
            System.out.println(temp);
        }
        
    }
    @FXML
    void getCalled(ActionEvent event) {
        String myMove = output.getText();
        String myCurrentDice = "";
        for(int i = 0; i<knownDice.length; i++){
            myCurrentDice += (knownDice[i] + " " + (i+1)+ "s\n\r");
        }
        output.setText("My move was: " + myMove +"\r\n"+ "MyDice: \n\r"+ myCurrentDice);
                
    }
    private boolean bluff(){
        int i = 2;
        int range = i*turn;
        Random rand = new Random();
        int value = rand.nextInt(range);
        if (value == 1){
            return true;
            
        }else{
            return false; 
        }
    }
    private int maxDiceNormalArray(int[] array){
        int maxAmount = 0;
        int maxValue = 0;
        
        for (int i = 0;i<knownDice.length;i++){
            if (knownDice[i]>maxAmount){
                maxAmount = knownDice[i];
                maxValue = (i+1);
                
            }
            
        }
        return maxValue;
    }
    private int minDiceNormalArray(int[] array){
        int maxAmount = 0;
        int maxValue = 0;
        
        for (int i = 0;i<knownDice.length;i++){
            if (knownDice[i]<=maxAmount){
                maxAmount = knownDice[i];
                maxValue = (i+1);
                
            }
            
        }
        return maxValue;
        
    }

    
}
