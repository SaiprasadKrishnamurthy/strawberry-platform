(function(angular) {

  var app = angular.module('exampleApp', [
    'angular-elastic-builder',
  ]);

  app.controller('BasicController', function() {

    var data = this.data = {};
    data.query = JSON.parse('{}');

    data.fields = JSON.parse(angular.element(document.querySelector('#fields')).val());

    data.needsUpdate = true;

    this.showQuery = function() {
      var queryToShow = {

            query: {
            filtered: {
               query: {
                match_all: {}
               },
               filter: {bool: { must : data.query } } } }
      };
      console.log(JSON.stringify(getObject(queryToShow)));
      return JSON.stringify(queryToShow, null, 2);
    };

    this.apply = function() {
        angular.element(parent.document.querySelector('#queryJson')).val(this.showQuery());
        alert(this.showQuery());
        parent.PF('lighbox1').hide();
    },
    this.cancel = function() {
        angular.element(document.querySelector('#query')).val(this.showQuery());
        parent.PF('lighbox1').hide();
     }
  });

function getObject(theObject) {
    var result = null;
    if(theObject instanceof Array) {
        for(var i = 0; i < theObject.length; i++) {
            result = getObject(theObject[i]);
            if (result) {
                break;
            }
        }
    }
    else
    {
        for(var prop in theObject) {
           console.log(" --> "+prop +" --> "+theObject);
            if(prop == 'must') {
                    return theObject;
            }
            if(theObject[prop] instanceof Object || theObject[prop] instanceof Array) {
                result = getObject(theObject[prop]);
                if (result) {
                    break;
                }
            }
        }
    }
    return result;
};

})(window.angular);