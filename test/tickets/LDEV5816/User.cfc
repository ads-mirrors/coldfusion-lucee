component {
    // Simple user CFC for type validation
    property name="username" type="string";
    property name="age" type="numeric";
    function init(string username, numeric age) {
        variables.username = arguments.username;
        variables.age = arguments.age;
        return this;
    }
    function getUsername() { return variables.username; }
    function getAge() { return variables.age; }
}