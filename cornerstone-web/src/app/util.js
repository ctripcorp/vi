var VIUtil = function() {
    var MB = 1024 * 1024;
    var M = 1000000;
    var G = 1000 * M;
    var GB = 1024 * MB;
    var TB = 1024 * GB;
    var HTMLESCAPEDIC = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    var fdDic = {
        'B': 'byte',
        'C': 'char',
        'D': 'double',
        'F': 'float',
        'I': 'int',
        'J': 'long',
        'S': 'short',
        'Z': 'boolean'
    };

    return {
        removeArrayItem: function(array, item) {

            var index = array.indexOf(item);
            if (index >= 0) {
                array.splice(index, 1);
                return true;
            }
            return false;

        },
        objToQuery: function(obj) {
            var str = [];
            for (var p in obj)
                if (obj.hasOwnProperty(p)) {
                    str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                }
            return str.join("&");
        },
        queryToObj: function(qstr) {
            var query = {};
            var a = (qstr[0] === '?' ? qstr.substr(1) : qstr).split('&');
            for (var i = 0; i < a.length; i++) {
                var b = a[i].split('=');
                query[decodeURIComponent(b[0])] = decodeURIComponent(b[1] || '');
            }
            return query;
        },
        calculateRunTime: function(millsec) {
            var d = new Date(millsec);
            var msec = d.getUTCMilliseconds();
            var sec = d.getUTCSeconds();
            var min = d.getUTCMinutes();
            var hour = d.getUTCHours();
            var day = d.getUTCDate() - 1;
            var month = d.getUTCMonth();
            var year = d.getFullYear() - 1970;
            var rtn = '';
            if (year > 0) {
                rtn += ' ' + year + ' year';
            }
            if (month > 0) {
                rtn += ' ' + month + ' month';
            }
            if (day > 0) {
                rtn += ' ' + day + ' day';
            }
            if (hour > 0) {
                rtn += ' ' + hour + ' hrs';
            }
            if (min > 0) {
                rtn += ' ' + min + ' min';
            }
            if (sec > 0) {
                rtn += ' ' + sec + ' sec';
            }
            if (msec > 0) {
                rtn += ' ' + msec + ' ms';
            }

            return rtn || Math.ceil(millsec) + 'ms';
        },
        formatBytes: function(d, type) {

            if (!type) {
                if (d > TB)
                    type = 'T';
                else if (d > GB)
                    type = 'G';
                else if (d > MB)
                    type = 'M';
                else if (d > 1024)
                    type = 'K';
            }
            var rtn = d;
            switch (type) {
                case 'T':
                    rtn = d3.format('#,.1f')(d / TB) + 'TB';
                    break;
                case 'G':
                    rtn = d3.format('#,.1f')(d / GB) + 'GB';
                    break;
                case 'M':
                    rtn = d3.format('#,.1f')(d / MB) + 'MB';
                    break;
                case 'm':
                    rtn = d3.format('#,.3f')(d / MB) + 'MB';
                    break;
                case 'K':
                    rtn = d3.format('#,.1f')(d / 1024) + 'KB';
                    break;
                default:
                    rtn = Math.round(d) + 'b';
                    break;

            }

            return rtn;

        },
        formatNumber: function(d) {

            if (d > G)
                return d3.format('#,.1f')(d / G) + 'G';
            else if (d > M)
                return d3.format('#,.1f')(d / M) + 'M';
            else if (d > 1024)
                return d3.format('#,.1f')(d / 1000) + 'K';
            else
                return d;
        },
        getNumberFromStr: function(str) {
            return str.replace(/[^\d^\.]/g, '') - 0;
        },
        arrayMax: function(array, func) {
            var max = 0;
            var maxItem;

            array.forEach(function(x) {

                var current = func(x);
                if (current > max) {

                    max = current;
                    maxItem = x;
                }
            });
            return maxItem;

        },
        noop: function(x) {
            return x;
        },
        escapeHTML: function(x) {

            return x.replace(/&|<|>|"|'/g, function(match) {

                return HTMLESCAPEDIC[match];

            });


        },
        uuid: function() {
            var d = Date.now();
            if (typeof performance !== 'undefined' && typeof performance.now === 'function') {
                d += performance.now(); //use high-precision timer if available
            }
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = (d + Math.random() * 16) % 16 | 0;
                d = Math.floor(d / 16);
                return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
            });
        },
        formatDesc: function(name, pat) {
            var cname;
            var pattern = pat || '[';



            var n = 0;
            for (var i = 0; i < name.length; i++) {

                if (name[i] == pattern) {
                    n++;
                } else {
                    break;
                }

            }
            if (name.length == n + 1) {
                return fdDic[name[n]] + '[]'.repeat(n);

            } else if (name[n] == 'L') {
                return name.substr(n + 1, (name.length - n - 2)) + '[ ]'.repeat(n);
            }
            return name;

        }
    };
}();
