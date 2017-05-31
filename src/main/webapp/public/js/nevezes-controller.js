var app=angular.module('app', ['ngResource']);

app.controller('ctrl', function ($scope, $resource) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === window.location.pathname;
    };

    var res = initResources($scope, $resource);

    $scope.nevezes = res.NevezesService.get();

    $scope.nevezesDone = function () {
        $scope.errorMessages = new Array();
        $scope.tavClass=null;
        $scope.nevClass=null;
        $scope.nemClass=null;
        $scope.evClass=null;
        $scope.emailClass=null;
        $scope.checkClass=null;
        if (isBlank($scope.tav)) {
            $scope.tavClass='has-error';
            $scope.errorMessages.push("A táv kitöltése kötelező!");
        }
        if (isBlank($scope.nev)) {
            $scope.nevClass='has-error';
            $scope.errorMessages.push("A név kitöltése kötelező!");
        }
        if (!$scope.nem) {
            $scope.nemClass='has-error';
            $scope.errorMessages.push("A nem kitöltése kötelező!");
        }
        var ev = parseInt($scope.ev);
        if (isNaN(ev) || ev < 1900 || ev > new Date().getFullYear()) {
            $scope.evClass='has-error';
            $scope.errorMessages.push("A születési év kitöltése kötelező!");
        }
        if (isBlank($scope.email)) {
            $scope.emailClass='has-error';
            $scope.errorMessages.push("Az email cím kitöltése kötelező!");
        }
        if (!$scope.check) {
            $scope.checkClass='has-error';
            $scope.errorMessages.push("A versenyszabályzat elolvasása kötelező!");
        }
        var recaptcha = document.getElementById('g-recaptcha-response').value;
        if (!recaptcha) {
            $scope.errorMessages.push("Bizonyítsd be, hogy nem vagy robot!");
        }
        if ($scope.errorMessages.length == 0) {
            var sendData = {
                tav: $scope.tav,
                nev: $scope.nev,
                nem: $scope.nem,
                ev: ev,
                egyesulet: emptyForBlank($scope.egyesulet),
                email: $scope.email,
                recaptcha: recaptcha
            };
            res.NevezesService.store(sendData);
        }
    };
});

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function emptyForBlank(data) {
    return !isBlank(data) ? data : "";
}
