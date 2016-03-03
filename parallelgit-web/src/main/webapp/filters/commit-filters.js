app.filter('personName', function() {
  return function(person) {
    return person.name;
  }
});

app.filter('personDate', function() {
  return function(person) {
    return person.timestamp;
  }
});