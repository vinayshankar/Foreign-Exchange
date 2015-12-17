angular.module('hierarchie')
  .config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.when('/', {
        templateUrl: 'app/views/charts.html',
        controller: 'MainCtrl'
      })
        .otherwise({
          redirectTo: '/'
        });
    }
  ]);