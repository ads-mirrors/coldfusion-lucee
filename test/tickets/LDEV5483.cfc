component extends="org.lucee.cfml.test.LuceeTestCase" labels="currency" {

	variables.testNumbers = [ -1,-1.11, -1111 ];
	variables.testLocales = [ "English (United States)", "english (uk)", "german (germany)"];

	function beforeAll(){
		setLocale( "English (United States)" );
	}

	function afterAll(){
		setLocale( "English (United States)" );
	}

	function run( testResults , testBox ) {

		describe( title='LDEV-5483 dollarformat, useBrackets' , body=function(){

			it( title='dollarFormat negative values', body=function() {
				loop array="#variables.testNumbers#" item="local.n" {
					var positive = dollarFormat( abs(n) );
					expect( dollarFormat(n) ).toBe( "(" & positive & ")" );
					expect( dollarFormat(number=n, useBrackets=false ) ).toBe( "-" & positive );
				}
			});
		});

		describe( title='LDEV-5483 lsCurrencyFormat, useBrackets', body=function(){
			it( title='lsCurrencyFormat negative values', body=function() {
				loop array="#variables.testLocales#" item="local.locale" {
					setLocale( locale );
					var info = getLocaleInfo();
					var symbol = info.currency.symbol;
					loop array="#variables.testNumbers#" item="local.n" {
						var negative = LSCurrencyFormat(number=n,locale=locale);
						var positive = LSCurrencyFormat(number=abs(n),locale=locale);
						expect( negative ).toBe( "(" & positive & ")" );
						expect( negative ).toInclude( symbol );

						var useBrackets = LSCurrencyFormat(number=n,locale=locale, useBrackets=false);
						expect( useBrackets ).tobe( "-" & positive );
					}
				}
			});
			it( title='LSEuroCurrencyFormat negative values', body=function() {
				loop array="#variables.testLocales#" item="local.locale" {
					setLocale( locale );
					setLocale( locale );
					var info = getLocaleInfo();
					var symbol = info.currency.symbol;
					loop array="#variables.testNumbers#" item="local.n" {
						var negative = LSEuroCurrencyFormat(number=n,locale=locale);
						var positive = LSEuroCurrencyFormat(number=abs(n),locale=locale);
						expect( negative ).toBe( "(" & positive & ")" );
						expect( negative ).toInclude( symbol );

						var useBrackets = LSEuroCurrencyFormat(number=n,locale=locale, useBrackets=false);
						expect( useBrackets ).tobe( "-" & positive );
					}
				}
			});
		});
	}
}