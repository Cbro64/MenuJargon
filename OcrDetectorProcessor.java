/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * TODO: Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> graphicOverlay;
    private HashMap<String, String> dictionary;
    private Set<String> dictSet;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    // TODO:  Once this implements Detector.Processor<TextBlock>, implement the abstract methods.
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                //Log.d("Processor", "Text detected! " + item.getValue());
                List<? extends Text> textLines = item.getComponents();
                for(Text currentLine : textLines) {
                    String lineText =" "+currentLine.getValue().toLowerCase()+" ";
                    lineText = lineText.replaceAll(":","");
                    for (String dictword : dictSet) {
                        int index = lineText.indexOf(" "+dictword.toLowerCase()+" ");
                        if(index >=0 ) {
                            int wordpos = numWords(lineText.substring(0,index+2));
                            int phraselength = numWords(dictword);
                            List<? extends Text> textWords = currentLine.getComponents();
                            Text firstword = textWords.get(wordpos-2);
                            Text lastword = textWords.get(wordpos+phraselength-3);
                            Log.d("Processor", "Text detected! " + dictword + " " + firstword.getValue()+" " + lastword.getValue());
                            RectF boundingbox = new RectF();
                            boundingbox.top = Math.min(firstword.getBoundingBox().top,lastword.getBoundingBox().top);
                            boundingbox.bottom = Math.max(firstword.getBoundingBox().bottom,lastword.getBoundingBox().bottom);
                            boundingbox.left = Math.min(firstword.getBoundingBox().left,lastword.getBoundingBox().left);
                            boundingbox.right = Math.max(firstword.getBoundingBox().right,lastword.getBoundingBox().right);
                            OcrGraphic graphic = new OcrGraphic(graphicOverlay, dictword, boundingbox);
                            graphicOverlay.add(graphic);
                        }
                    }

//                    List<? extends Text> textWords = currentLine.getComponents();
//                    for(Text currentWord : textWords) {
//                        if(dictionary.containsKey(currentWord.getValue().toLowerCase())){
//                            OcrGraphic graphic = new OcrGraphic(graphicOverlay, currentWord);
//                            graphicOverlay.add(graphic);
//                        }
//                    }
                }
            }
        }
    }

    public void setDict(HashMap<String, String> dictionary) {
        this.dictionary = dictionary;
        this.dictSet = dictionary.keySet();
    }

    @Override
    public void release() {
        graphicOverlay.clear();
    }

    public static int numWords(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        String[] words = input.split("\\s+");
        return words.length;
    }
}
