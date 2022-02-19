package com.google.vision.linesegmentation;

import com.google.common.io.Resources;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GoogleVisionLineSegmentationParserTest {
    @Test
    public void happyPathParse() throws IOException {
        GoogleVisionLineSegmentationParser googleVisionLineSegmentationParser = new GoogleVisionLineSegmentationParser();
        URL url =  this.getClass().getResource("/S01200HQT173.jpg.json");
        String content = Resources.toString(url, StandardCharsets.UTF_8);
        AnnotateImageResponse annotateImageResponse = getGson().fromJson(content, AnnotateImageResponse.class);
        List<String> mergedArray = googleVisionLineSegmentationParser.initLineSegmentation(annotateImageResponse);

        String[] expected = new String[]{"TESCO",
                "eactra",
                "CUMBERNAULD 0345 6779808",

                "KITTEN FOOD £3.50",
                "DIPPERS £2.50",
                "CKN DIPPERS £1.50",
                "CRISPS £1.24",
                "MINI CHEDDAR £1.00",
                "CRISPS £1.24",
                "MINI CHEDDAR £1.00",
                "T POTATO CAKES eU",
                "£0.50 £1.00",
                "CKN/MUSH SLICE £1.50",
                "CKN FINGERS £2.00",
                "TARTS £1.00",
                "CKN FINGERS £2.00",
                "TARTS £1.00",
                "SCONES £1.20",
                "BS 4 BURG BUNS 0.70",
                "£0.80",
                "BIN LINERS x £1.00",
                "KM SOFT THICK",
                "SWEETS",
                "£0.40 0.80",
                "GOV BAG CHARGE+x £0.05",
                "BISCUITS £1.00",
                "£1.29",
                "TIMEOUT",
                "TOTAL £27.32",
                "CASH",
                "CHANGE DUE £2.68",
                "830.00",
                "CLUBCARD STATEMENT",
                "CLUBCARD NUMBER xxxxxxxxxxxxxx0582",
                "QUALIFVING SPEND £27.27",
                "POINTS THIS VISIT 27",
                "TOTAL UP TO 07/11/16",
                "TESCO",
                "Bran",
                "Guarantee",
                "Today we were",
                "E O.35",
                "cheaper",
                "on your branded basket compared to",
                "Asda, Morrisons and Sainsbury's",
                "Our Brand Guarantee instantly matches",
                "your branded basket so you can always",
                "checkout with confi dence",
                "Branded grocery basket matched. For full",
                "terms visit tesco.com/brandguar antee",
                "by telling us about your trip",
                "at www.tescoviews.com",
                "and collect 25 Clubcard points.",
                "Terms and conditions apply"};
        Assert.assertArrayEquals(expected, mergedArray.toArray(new String[0]));
    }

    @Test
    public void maxYTest() {
        GoogleVisionLineSegmentationParser googleVisionLineSegmentationParser = new GoogleVisionLineSegmentationParser();
        EntityAnnotation entityAnnotation = EntityAnnotation.newBuilder().setBoundingPoly(BoundingPoly.newBuilder()
                .addVertices(Vertex.newBuilder().setX(262).setY(260).build())
                .addVertices(Vertex.newBuilder().setX(1176).setY(260).build())
                .addVertices(Vertex.newBuilder().setX(1176).setY(3486).build())
                .addVertices(Vertex.newBuilder().setX(262).setY(3486).build()).build()).build();
        Assert.assertEquals(googleVisionLineSegmentationParser.getYMax(entityAnnotation), 3486);

    }

    Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingStrategy(new PrivateFieldNamingStrategy())
                .setPrettyPrinting().disableHtmlEscaping()
                .create();
    }

    class PrivateFieldNamingStrategy implements FieldNamingStrategy {

        @Override
        public String translateName(Field f) {
            String fieldName = FieldNamingPolicy.IDENTITY.translateName(f);
            if (fieldName.endsWith("_")) {
                return fieldName.substring(0, fieldName.length() - 1);
            }
            return fieldName;
        }
    }
}
