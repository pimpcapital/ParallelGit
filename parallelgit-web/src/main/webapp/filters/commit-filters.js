app.filter('hashAbbreviation', function() {
  return function(hash) {
    return hash.substring(0, 7);
  }
});

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