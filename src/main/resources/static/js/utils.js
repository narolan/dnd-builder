// Reads the XSRF-TOKEN cookie set by Spring Security and injects it into
// every POST/PUT/DELETE fetch call so CSRF protection passes server-side.
function securedFetch(url, options) {
    options = options || {};
    var raw = document.cookie.split('; ').find(function(c) { return c.startsWith('XSRF-TOKEN='); });
    var token = raw ? decodeURIComponent(raw.split('=')[1]) : null;
    return fetch(url, Object.assign({}, options, {
        headers: Object.assign({}, options.headers || {}, token ? { 'X-XSRF-TOKEN': token } : {})
    }));
}
