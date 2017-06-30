var app=angular.module('app', ['ngResource']);

app.controller('ctrl', function ($scope, $resource) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === window.location.pathname;
    };

    var res = initResources($scope, $resource);

    $scope.athletesList = res.NevezesService.getListOfAthletes();

});
