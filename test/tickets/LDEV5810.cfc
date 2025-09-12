component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {
		describe("LDEV-5810: Batik SVG transcoding fails with Java module access error", function() {
			
			it("should be able to use Batik PNGTranscoder with javasettings maven dependencies", function() {
				// Test case for regression introduced between 7.0.0.372 and 7.0.0.377
				// Batik SVG transcoding fails with:
				// "java.xml does not export com.sun.org.apache.xerces.internal.jaxp to unnamed module"
				
				// Create simple test SVG content
				var testSvgContent = '<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
					<circle cx="50" cy="50" r="40" fill="blue" />
				</svg>';
				
				// Create temp files for test
				var tempDir = getTempDirectory() & "ldev5810-test/";
				if (!directoryExists(tempDir)) {
					directoryCreate(tempDir);
				}
				
				var svgPath = tempDir & "test.svg";
				var pngPath = tempDir & "test.png";
				
				// Write test SVG file
				fileWrite(svgPath, testSvgContent);
				
				// Create SVG renderer component with Maven dependencies (same as forum post)
				var svgRenderer = new component javasettings='{
					maven: [
						"org.apache.xmlgraphics:batik-transcoder:1.18",
						"org.apache.xmlgraphics:batik-codec:1.18"
					]
				}' {
					import java.io.FileInputStream;
					import java.io.FileOutputStream;
					import java.io.File;
					import org.apache.batik.transcoder.TranscoderInput;
					import org.apache.batik.transcoder.TranscoderOutput;
					import org.apache.batik.transcoder.image.PNGTranscoder;
					
					public function render(required string svgPath, required string outputPath) {
						var transcoder = new PNGTranscoder();
						var input = new TranscoderInput(new FileInputStream(new File(svgPath)));
						var outputStream = new FileOutputStream(outputPath);
						try {
							var output = new TranscoderOutput(outputStream);
							transcoder.transcode(input, output);
							outputStream.flush();
						} finally {
							outputStream.close();
						}
					}
				};
				
				svgRenderer.render(svgPath, pngPath);
				// This should work without throwing Java module access errors
				expect(function() {
					svgRenderer.render(svgPath, pngPath);
				}).notToThrow();

				//Failed: The incoming function DID throw an exception of type [java.lang.ClassCastException] with message [class com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl cannot be cast to class javax.xml.parsers.SAXParserFactory (com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl is in module java.xml of loader 'bootstrap'; javax.xml.parsers.SAXParserFactory is in unnamed module of loader lucee.commons.lang.PhysicalClassLoader @472196b3)] detail []


				
				// Verify PNG was created
				expect(fileExists(pngPath)).toBeTrue("PNG file should be created");
				
				if (fileExists(pngPath)) {
					var fileSize = getFileInfo(pngPath).size;
					expect(fileSize).toBeGT(0, "PNG file should not be empty");
				}
				
				// Cleanup
				if (directoryExists(tempDir)) {
					directoryDelete(tempDir, true);
				}
			});
		});
	}
}