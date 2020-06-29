function initResources($scope, $resource) {
    let defaultServerError = function () {
        $scope.errorMessages.push('Hoppá, valami hiba történt :-(');
    };

    let resource = {};

    resource.NevezesService = $resource('/public/nevezes/:action', {}, {
        get: {params: {action: 'get', id: $scope.id}, interceptor: {responseError : defaultServerError}},
        getListOfAthletes: {params: {action: 'getListOfAthletes', id: $scope.id}, interceptor: {responseError : defaultServerError}},
        store: {method: 'POST', params: {action: 'store'}, interceptor: {responseError : defaultServerError}},
        subscribe: {method: 'POST', params: {action: 'subscribe'}, interceptor: {responseError : defaultServerError}}
    });

    return resource;
}

function parseQuery(queryString) {
    var query = {};
    var pairs = (queryString[0] === '?' ? queryString.substr(1) : queryString).split('&');
    for (var i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split('=');
        query[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || '');
    }
    return query;
}
