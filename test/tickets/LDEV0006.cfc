component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	
	public void function testImageGetEXIFMetadata() {
		var img = "LDEV0006/exif_original.jpg";
		var imgObj = imageRead(img);
		var imgMeta = imageGetEXIFMetadata(imgObj);
		expect( structCount( imgMeta ) ).toBe( isVersion2() ? 185 : 124);
	}

	private boolean function isVersion2(){
		var qry = extensionlist(false);
		loop query=qry {
			if(qry.id=="B03E92E1-F2F3-4380-981922D0BDFEF2B8") {
				if(left(qry.version,1)>=2) return true;
			}
		}
		return false;
	}
}