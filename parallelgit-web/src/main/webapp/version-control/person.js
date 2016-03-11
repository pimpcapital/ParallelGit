app.factory('Person', function() {
  function Person(attributes) {
    this.name = attributes.name;
    this.email = attributes.email;
    this.timestamp = new Date(attributes.timestamp);
  }

  return Person;
});