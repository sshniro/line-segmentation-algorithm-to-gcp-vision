# Line Segmentation — Kotlin

A Kotlin/JVM port of the line-segmentation algorithm. It post-processes a Google Cloud Vision
OCR response and returns the text as an ordered list of lines, reconstructing reading order for
sparse documents (receipts/invoices).

> **Note:** this is a 2018-era port, kept as a reference implementation. For new projects, see
> [*Is there a better way today?*](../README.md#is-there-a-better-way-today) in the root README.

## Requirements

- JDK 8+
- Dependencies (resolved from Maven Central): `com.google.cloud:google-cloud-vision` and
  `com.google.code.gson:gson` — see [`build.gradle`](./build.gradle).

## Input

The parser works on a Vision `com.google.cloud.vision.v1.AnnotateImageResponse` whose
`textAnnotationsList` contains the full-text summary block at index `0` followed by the
individual words. It does not call Google Vision itself — you supply the response.

## Usage

```kotlin
import com.google.cloud.vision.v1.AnnotateImageResponse

// Deserialize a saved Vision JSON response into AnnotateImageResponse.
// Vision's protobuf JSON uses trailing-underscore field names, so Gson must be configured with a
// FieldNamingStrategy that strips them. `getGson()` below is the helper from the test class
// (GoogleVisionLineSegmentationParserTest) — reuse that exact configuration.
val response: AnnotateImageResponse =
    getGson().fromJson(jsonString, AnnotateImageResponse::class.java)

val parser = GoogleVisionLineSegmentationParser()
val lines: List<String> = parser.initLineSegmentation(response)
// -> ["KITTEN FOOD £3.50", "DIPPERS £2.50", "CKN DIPPERS £1.50", ...]
```

See [`src/test/kotlin/.../GoogleVisionLineSegmentationParserTest.kt`](./src/test/kotlin/com/google/vision/linesegmentation/GoogleVisionLineSegmentationParserTest.kt)
for a complete, runnable example including the Gson configuration used to deserialize the
Vision protobuf JSON.

## Test

```
./gradlew test
```

## Note on the package name

The source currently lives under the `com.google.vision.linesegmentation` package. That
namespace belongs to Google; if this code were ever published as an artifact it should be moved
under your own group id (e.g. `io.github.sshniro`). It is left unchanged here to preserve the
original source.
