component {
	public function init() {
		return this;
	}

	public function log( required string message ) {
		systemOutput( arguments.message, true );
	}
}
