var app=angular.module('app', ['ngResource']);

app.controller('ctrl', function ($scope, $resource, $window) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === $window.location.pathname;
    };
    $scope.id = parseQuery($window.location.search).id;

    var res = initResources($scope, $resource);

    $scope.athletesList = res.NevezesService.getListOfAthletes();
});
