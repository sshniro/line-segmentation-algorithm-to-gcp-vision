package com.google.vision.linesegmentation;


import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.Gson;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.round;

/**
 * @author Stony on 2/18/22 2:20 PM.
 */
public class GoogleVisionLineSegmentationParser {
    List<String> initLineSegmentation(AnnotateImageResponse data) {
        int yMax = getYMax(data.getTextAnnotations(0));

        AnnotateImageResponse newData = invertAxis(data, yMax);
        // The first index refers to the auto identified words which belongs to a sings line
        List<String> lines = new ArrayList<>(Arrays.asList(newData.getTextAnnotationsList().get(0).getDescription().split("\n")));
        // gcp vision full text
        List<EntityAnnotation> rawText = new ArrayList<>(deepCopy(newData).getTextAnnotationsList());

                // reverse to use lifo, because array.shift() will consume 0(n)
        Collections.reverse(lines);
        Collections.reverse(rawText);
//        lines = lines.reversed().toMutableList();
//        rawText = rawText.reversed().toMutableList();
        //to remove the zeroth element which gives the total summary of the text
        rawText.remove(rawText.size() - 1);

        List<EntityAnnotation>  mergedArray = getMergedLines(lines, rawText);
        List<Pair<EntityAnnotation, EntityMetadata>>  entityToMetadata = getBoundingPolygon(mergedArray);

        combineBoundingPolygon(entityToMetadata);
        return constructLineWithBoundingPolygon(entityToMetadata);
    }

    // TODO implement the line ordering for multiple words
    protected  List<String>  constructLineWithBoundingPolygon(List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata){
        ArrayList<String> finalArray = new ArrayList<>();

        for (int index = 0; index < entityToMetadata.size(); index++) {
            Pair<EntityAnnotation, EntityMetadata> it = entityToMetadata.get(index);

            if (!it.getSecond().matched) {
                if (it.getSecond().match.size() == 0) {
                    finalArray.add(it.getFirst().getDescription());
                } else {
                    // arrangeWordsInOrder(mergedArray, i);
                    // let index = mergedArray[i]['match'][0]['matchLineNum'];
                    // let secondPart = mergedArray[index].description;
                    // finalArray.push(mergedArray[i].description + ' ' +secondPart);
                    finalArray.add(arrangeWordsInOrder(entityToMetadata, index));
                }
            }
        }
        return finalArray;
    }

    private List<EntityAnnotation> getMergedLines(List<String> lines, List<EntityAnnotation> rawText) {
        ArrayList<EntityAnnotation> mergedArray = new ArrayList<>();
        while (lines.size() != 0) {
            String l = lines.remove(lines.size() - 1);
            String l1 = l;
            boolean status = true;
            EntityAnnotation mergedElement = null;

            while (true) {
                if (rawText.isEmpty()) {
                    break;
                }
                EntityAnnotation wElement = rawText.remove(rawText.size() - 1);
                String w = wElement.getDescription();

                int index = l.indexOf(w);

                l = l.substring(index + w.length());

                if (status) {
                    status = false;
                    // set starting coordinates
                    mergedElement = wElement;
                }
                if (l.equals("")) {
                    EntityAnnotation newElement = EntityAnnotation.newBuilder().mergeFrom(mergedElement)
                            .setDescription(l1)
                            .setBoundingPoly(BoundingPoly.newBuilder().mergeFrom(mergedElement.getBoundingPoly()).clearVertices()
                            .addVertices(0, mergedElement.getBoundingPoly().getVerticesList().get(0))
                            .addVertices(1, wElement.getBoundingPoly().getVerticesList().get(1))
                            .addVertices(2, wElement.getBoundingPoly().getVerticesList().get(2))
                            .addVertices(3, mergedElement.getBoundingPoly().getVerticesList().get(3)).build()).build();
                    mergedArray.add(newElement);
                    break;
                }
            }
        }
        return mergedArray;
    }

    private String arrangeWordsInOrder(List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata, int k) {
        String mergedLine = "";
        List<Match> line = entityToMetadata.get(k).getSecond().match;

        for (Match it : line) {
            int index = it.matchLineNum;
            String matchedWordForLine = entityToMetadata.get(index).getFirst().getDescription();

            int mainX = entityToMetadata.get(k).getFirst().getBoundingPoly().getVerticesList().get(0).getX();
            int compareX = entityToMetadata.get(index).getFirst().getBoundingPoly().getVerticesList().get(0).getX();

            if (compareX > mainX) {
                mergedLine = entityToMetadata.get(k).getFirst().getDescription() + ' ' + matchedWordForLine;
            } else {
                mergedLine = matchedWordForLine + ' ' + entityToMetadata.get(k).getFirst().getDescription();
            }
        }
        return mergedLine;
    }

    /**
     * @Method computes the maximum y coordinate from the identified text blob
     * @param data
     * @returns {*}
     */
    int getYMax(EntityAnnotation data) {
        Optional<Vertex> max = data.getBoundingPoly().getVerticesList().stream().max(Comparator.comparingInt(Vertex::getY));
        return max.get().getY();
    }

    /**
     * @Method inverts the y axis coordinates for easier computation
     * as the google vision starts the y axis from the bottom
     * @param data
     * @param yMax
     * @returns {*}
     */
    private AnnotateImageResponse invertAxis(AnnotateImageResponse data, int yMax) {
        //TODO Don't think this is needed
        //data = fillMissingValues(data);
        List<EntityAnnotation> newEntities = new ArrayList<>();

        newEntities.add(data.getTextAnnotations(0));
        for (int i=1;i<data.getTextAnnotationsCount();i++) {
            List<Vertex> vertexList = new ArrayList<>();
            data.getTextAnnotations(i).getBoundingPoly().getVerticesList().forEach( it ->{
                vertexList.add(Vertex.newBuilder().mergeFrom(it).clearY().setY(yMax - it.getY()).build());
            });

            EntityAnnotation.Builder entityBuilder = EntityAnnotation.newBuilder().mergeFrom(data.getTextAnnotations(i));
            entityBuilder.setBoundingPoly( entityBuilder.getBoundingPolyBuilder().clearVertices().addAllVertices(vertexList).build());
            EntityAnnotation newEntity = entityBuilder.build();
            newEntities.add(newEntity);
        }
        AnnotateImageResponse.Builder responseBuilder = AnnotateImageResponse.newBuilder().mergeFrom(data);
        responseBuilder.clearTextAnnotations();
        responseBuilder.addAllTextAnnotations(newEntities);
        return responseBuilder.build();
    }


    /**
     *
     * @param mergedArray
     */
    private List<Pair<EntityAnnotation, EntityMetadata>> getBoundingPolygon(List<EntityAnnotation> mergedArray) {
        List<Pair<EntityAnnotation, EntityMetadata>> entityAnnotationToMetadata = new ArrayList<>();

        for (int index = 0; index < mergedArray.size(); index++) {
            EntityAnnotation it = mergedArray.get(index);
            ArrayList<Vertex> arr = new ArrayList<>();
            // calculate line height
            int h1 = it.getBoundingPoly().getVerticesList().get(0).getY() - it.getBoundingPoly().getVerticesList().get(3).getY();
            int h2 = it.getBoundingPoly().getVerticesList().get(1).getY() - it.getBoundingPoly().getVerticesList().get(2).getY();
            int h = h1;
            if (h2 > h1) {
                h = h2;
            }
            double avgHeight = h * 0.6;

            arr.add(it.getBoundingPoly().getVerticesList().get(1));
            arr.add(it.getBoundingPoly().getVerticesList().get(0));
            Rectangle line1 = getRectangle(arr, avgHeight, true);

            arr.clear();
            arr.add(it.getBoundingPoly().getVerticesList().get(2));
            arr.add(it.getBoundingPoly().getVerticesList().get(3));
            Rectangle line2 = getRectangle(arr, avgHeight, false);

            entityAnnotationToMetadata.add(new Pair<>(it, new EntityMetadata(createPolygon(line1, line2), index, new ArrayList<>(), false)));
        }
        return entityAnnotationToMetadata;
    }

    private void combineBoundingPolygon( List<Pair<EntityAnnotation, EntityMetadata>> entityToMetadata) {
        // select one word from the array
        for (int index1 = 0; index1 < entityToMetadata.size(); index1++) {
            Pair<EntityAnnotation, EntityMetadata> it = entityToMetadata.get(index1);

            Polygon bigBB = it.getSecond().bigBB;
            // iterate through all the array to find the match
            for (int index2 = index1; index2 < entityToMetadata.size(); index2++) {

                Pair<EntityAnnotation, EntityMetadata> k = entityToMetadata.get(index2);
                // Do not compare with the own bounding box and which was not matched with a line
                if (index1 != index2 && !k.getSecond().matched) {
                    int insideCount = 0;
                    for (Vertex coordinate : k.getFirst().getBoundingPoly().getVerticesList()) {
                        if (bigBB.contains(coordinate.getX(), coordinate.getY())) {
                            insideCount += 1;
                        }
                    }

                    // all four point were inside the big bb
                    if (insideCount == 4) {
                        it.getSecond().match.add(new Match(insideCount, index2));
                        k.getSecond().matched = true;
                    }

                }
            }
        }
    }

    private Rectangle getRectangle(List<Vertex> v, Double avgHeight, Boolean isAdd) {
        double firstCandidate;
        double secondCandidate;
        if (isAdd) {
            secondCandidate = v.get(1).getY() + avgHeight;
            firstCandidate = v.get(0).getY()+ avgHeight;
        } else {
            secondCandidate = v.get(1).getY()- avgHeight;
            firstCandidate = v.get(0).getY() - avgHeight;
        }

        double yDiff = (secondCandidate - firstCandidate);
        int xDiff = (v.get(1).getX() - v.get(0).getX());

        double gradient = yDiff / xDiff;

        int xThreshMin = 1;
        int xThreshMax = 2000;

        double yMin;
        double yMax;
        if (gradient == 0.0) {
            // extend the line
            yMin = firstCandidate;
            yMax = firstCandidate;
        } else {
            yMin = (firstCandidate) - (gradient * (v.get(0).getX() - xThreshMin));
            yMax = (firstCandidate) + (gradient * (xThreshMax - v.get(0).getX()));
        }

        return new Rectangle(xThreshMin, xThreshMax, round(yMin), round(yMax));
    }

    private Polygon createPolygon(Rectangle line1, Rectangle line2) {
        Polygon polygon = new Polygon();
        polygon.addPoint(line1.xMin, round(line1.yMin));
        polygon.addPoint(line1.xMax, round(line1.yMax));
        polygon.addPoint(line2.xMax, round(line2.yMax));
        polygon.addPoint(line2.xMin, round(line2.yMin));
        return polygon;
    }

    private AnnotateImageResponse deepCopy(AnnotateImageResponse t) {
        String serializedObj = new Gson().toJson(t);
        return new Gson().fromJson(serializedObj, AnnotateImageResponse.class);
    }

    static class Rectangle {
        int xMin;
        int xMax;
        long yMin;
        long yMax;

        public Rectangle(int xMin, int xMax, long yMin, long yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }
    }

    static class EntityMetadata {
        Polygon bigBB;
        int lineNum;
        List<Match> match;
        Boolean matched;

        public EntityMetadata(Polygon bigBB, int lineNum, List<Match> match, Boolean matched) {
            this.bigBB = bigBB;
            this.lineNum = lineNum;
            this.match = match;
            this.matched = matched;
        }
    }

     static class Match {
         int matchCount;
         int matchLineNum;

         public Match(int matchCount, int matchLineNum) {
             this.matchCount = matchCount;
             this.matchLineNum = matchLineNum;
         }
     }

    static class Pair<K, V> {
        K first;
        V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public K getFirst() {
            return first;
        }

        public V getSecond() {
            return second;
        }

    }
}
