function initResources($scope, $resource) {
    var defaultServerError = function () {
        $scope.errorMessages.push('Hoppá, valami hiba történt :-(');
    };

    var resource = new Object();

    resource.NevezesService = $resource('/public/nevezes/:action', {}, {
        get: {params: {action: 'get'}, interceptor: {responseError : defaultServerError}},
        getListOfAthletes: {params: {action: 'getListOfAthletes'}, interceptor: {responseError : defaultServerError}},
        store: {method: 'POST', params: {action: 'store'}, interceptor: {responseError : defaultServerError}}
    });

    return resource;

}