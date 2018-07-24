package me.ryanhamshire.GriefPrevention;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

public class Tests
{
   @Test
   public void TrivialTest()
   {
       assertTrue(true);
   }
   
   @Test
   public void WordFinder_BeginningMiddleEnd()
   {
       WordFinder finder = new WordFinder(Arrays.asList("alpha", "beta", "gamma"));
       assertTrue(finder.hasMatch("alpha"));
       assertTrue(finder.hasMatch("alpha etc"));
       assertTrue(finder.hasMatch("etc alpha etc"));
       assertTrue(finder.hasMatch("etc alpha"));
       
       assertTrue(finder.hasMatch("beta"));
       assertTrue(finder.hasMatch("beta etc"));
       assertTrue(finder.hasMatch("etc beta etc"));
       assertTrue(finder.hasMatch("etc beta"));
       
       assertTrue(finder.hasMatch("gamma"));
       assertTrue(finder.hasMatch("gamma etc"));
       assertTrue(finder.hasMatch("etc gamma etc"));
       assertTrue(finder.hasMatch("etc gamma"));
   }
   
   @Test
   public void WordFinder_Casing()
   {
       WordFinder finder = new WordFinder(Arrays.asList("aLPhA"));
       assertTrue(finder.hasMatch("alpha"));
       assertTrue(finder.hasMatch("aLPhA"));
       assertTrue(finder.hasMatch("AlpHa"));
       assertTrue(finder.hasMatch("ALPHA"));
   }
   
   @Test
   public void WordFinder_Punctuation()
   {
       WordFinder finder = new WordFinder(Arrays.asList("alpha"));
       assertTrue(finder.hasMatch("What do you think,alpha?"));
   }
   
   @Test
   public void WordFinder_NoMatch()
   {
       WordFinder finder = new WordFinder(Arrays.asList("alpha"));
       assertFalse(finder.hasMatch("Unit testing is smart."));
   }
   
   @Test
   public void WordFinder_EmptyList()
   {
       WordFinder finder = new WordFinder(new ArrayList<String>());
       assertFalse(finder.hasMatch("alpha"));
   }
   
   @Test
   public void WordFinder_PunctuationOnly()
   {
       WordFinder finder = new WordFinder(Arrays.asList("alpha"));
       assertFalse(finder.hasMatch("!"));
       assertFalse(finder.hasMatch("?"));
   }
   
   @Test
   public void WordFinder_StartingPunctuation()
   {
       WordFinder finder = new WordFinder(Arrays.asList("alpha"));
       assertFalse(finder.hasMatch("!asas dfasdf"));
       assertFalse(finder.hasMatch("?asdfa sdfas df"));
   }
   
   private UUID player1 = UUID.fromString("f13c5a98-3777-4659-a111-5617adb7d7fb");
   private UUID player2 = UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7");
      
   @Test
   public void SpamDetector_BasicChatOK()
   {
       SpamDetector detector = new SpamDetector();
       String message = "Hi, everybody! :)";
       SpamAnalysisResult result = detector.AnalyzeMessage(player1, message, 1000);
       assertTrue(result.muteReason == null);
       assertFalse(result.shouldWarnChatter);
       assertFalse(result.shouldBanChatter);
       assertTrue(result.finalMessage.equals(message));
   }  
   
   @Test
   public void SpamDetector_CoordinatesOK()
   {
       SpamDetector detector = new SpamDetector();
       assertTrue(detector.AnalyzeMessage(player1, "1029,2945", 0).muteReason == null);
       assertTrue(detector.AnalyzeMessage(player1, "x1029 z2945", 100000).muteReason == null);
       assertTrue(detector.AnalyzeMessage(player1, "x=1029; y=60; z=2945", 200000).muteReason == null);
   }
   
   @Test
   public void SpamDetector_NumbersOK()
   {
       SpamDetector detector = new SpamDetector();
       assertTrue(detector.AnalyzeMessage(player1, "25", 0).muteReason == null);
       assertTrue(detector.AnalyzeMessage(player1, "12,234.89", 100000).muteReason == null);
       assertTrue(detector.AnalyzeMessage(player1, "20078", 200000).muteReason == null);
   }
   
   @Test
   public void SpamDetector_RepetitionExact()
   {
       SpamDetector detector = new SpamDetector();
       String message = "Hi, everybody!   :)";
       assertFalse(detector.AnalyzeMessage(player1, message, 1000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, message, 28000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_RepetitionExactOK()
   {
       SpamDetector detector = new SpamDetector();
       String message = "Hi, everybody!   :)";
       assertFalse(detector.AnalyzeMessage(player1, message, 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, message, 35000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_Padding()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Hacking is really fun guys!! :) 123123123456.12398127498762935", 1000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Hacking is really fun guys!! :) 112321523456.1239345498762935", 1000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_Repetition()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody!   :)", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody!    :)", 5000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Hi, everybody!   :)", 9000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_TeamRepetition()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 1000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player2, "Hi, everybody! :)", 2500).muteReason != null);
   }
   
   @Test
   public void SpamDetector_TeamRepetitionOK()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "hi", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player2, "hi", 3000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_Gibberish()
   {
       SpamDetector detector = new SpamDetector();
       assertTrue(detector.AnalyzeMessage(player1, "poiufpoiuasdfpoiuasdfuaufpoiasfopiuasdfpoiuasdufsdf", 1000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player2, "&^%(& (&^%(%    (*%#@^ #$&(_||", 3000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_RepetitionOK()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody!   :)", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody!    :)", 12000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_TooFast()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "How's it going? :)", 2000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 3000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 4000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_TooMuchVolume()
   {
       SpamDetector detector = new SpamDetector();
       assertFalse(detector.AnalyzeMessage(player1, "Once upon a time there was this guy who wanted to be a hacker.  So he started logging into Minecraft servers and threatening to DDOS them.", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Everybody knew that he couldn't be a real hacker, because no real hacker would consider hacking Minecraft to be worth their time, but he didn't understand that even after it was explained to him.", 3000).muteReason != null);
       
       //start of mute
       assertTrue(detector.AnalyzeMessage(player1, "After I put him in jail and he wasted half an hour of his time trying to solve the (unsolvable) jail 'puzzle', he offered his services to me in exchange for being let out of jail.", 10000).muteReason != null);
       
       //forgiven after taking a break
       assertFalse(detector.AnalyzeMessage(player1, "He promised to DDOS any of my 'rival servers'.  So I offered him an opportunity to prove he could do what he said, and I gave him his own IP address from our server logs.  Then he disappeared for a while.", 16000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "When he finally came back, I /SoftMuted him and left him in the jail.", 28000).muteReason != null);
   }
   
   @Test
   public void SpamDetector_Caps()
   {
       SpamDetector detector = new SpamDetector();
       String message = "OMG I LUFF U KRISTINAAAAAA!";
       SpamAnalysisResult result = detector.AnalyzeMessage(player1, message, 1000);
       assertTrue(result.finalMessage.equals(message.toLowerCase()));
       assertTrue(result.muteReason == null);
   }
   
   @Test
   public void SpamDetector_CapsOK()
   {
       SpamDetector detector = new SpamDetector();
       String message = "=D";
       SpamAnalysisResult result = detector.AnalyzeMessage(player1, message, 1000);
       assertTrue(result.finalMessage.equals(message));
       assertTrue(result.muteReason == null);
   }
   
   @Test
   public void SpamDetector_WarnAndBan()
   {
       SpamDetector detector = new SpamDetector();
       
       //allowable noise
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "How's it going? :)", 2000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 3000).muteReason != null);
       
       //begin mute and warning
       SpamAnalysisResult result = detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 4000);
       assertTrue(result.muteReason != null);
       assertTrue(result.shouldWarnChatter);
       assertFalse(result.shouldBanChatter);
       assertTrue(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 5000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 6000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 7000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "How's it going? :)", 8000).muteReason != null);
       
       //ban
       result = detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 9000);
       assertTrue(result.shouldBanChatter);
   }
   
   @Test
   public void SpamDetector_Forgiveness()
   {
       SpamDetector detector = new SpamDetector();
       
       //allowable noise
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 1000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "How's it going? :)", 2000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 3000).muteReason != null);
       
       //start of mutes, and a warning
       SpamAnalysisResult result = detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 4000);
       assertTrue(result.muteReason != null);
       assertTrue(result.shouldWarnChatter);
       assertFalse(result.shouldBanChatter);
       assertTrue(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 5000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 6000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 7000).muteReason != null);
       assertTrue(detector.AnalyzeMessage(player1, "How's it going? :)", 8000).muteReason != null);
       
       //long delay before next message, not muted anymore
       result = detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 20000);
       assertFalse(result.shouldBanChatter);
       assertFalse(detector.AnalyzeMessage(player1, "Hi, everybody! :)", 21000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "How's it going? :)", 22000).muteReason != null);
       assertFalse(detector.AnalyzeMessage(player1, "Oh how I've missed you all! :)", 23000).muteReason != null);
       
       //mutes start again, and warning appears again
       result = detector.AnalyzeMessage(player1, "Why is nobody responding to me??!", 24000);
       assertTrue(result.muteReason != null);
       assertTrue(result.shouldWarnChatter);
       assertFalse(result.shouldBanChatter);
   }
}