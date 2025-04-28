component accessors=true{
	property name="name" type="string" getter="true" setter="true" default="lucee";
	property name="id" type="numeric" getter="true" setter="true" default="#123#";
	property name="luceeRocks" type="boolean" getter="true" setter="true" default="#true#";
	function getInstance(){
		return this;
	}
};