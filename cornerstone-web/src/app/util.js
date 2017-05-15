var VIUtil = function() {
    var MB = 1024 * 1024;
    var M = 1000000;
    var G = 1000 * M;
    var GB = 1024 * MB;
    return {
        removeArrayItem: function(array, item) {

            var index = array.indexOf(item);
            if (index >= 0) {
                array.splice(index, 1);
                return true;
            }
            return false;

        },
        calculateRunTime: function(millsec) {
            var d = new Date(millsec);
            var sec = d.getUTCSeconds();
            var min = d.getUTCMinutes();
            var hour = d.getUTCHours();
            var day = d.getUTCDate() - 1;
            var month = d.getUTCMonth();
            var year = d.getFullYear() - 1970;
            var rtn = '';
            if (year > 0) {
                rtn += year + '年';
            }
            if (month > 0) {
                rtn += month + '月';
            }
            if (day > 0) {
                rtn += day + '天';
            }
            if (hour > 0) {
                rtn += hour + '小时';
            }
            if (min > 0) {
                rtn += min + '分钟';
            }
            if (sec > 0) {
                rtn += sec + '秒';
            }
            return rtn;
        },
        formatBytes: function(d, type) {

            if (!type) {
                if (d > GB)
                    type = 'G';
                else if (d > MB)
                    type = 'M';
                else if (d > 1024)
                    type = 'K';
            }
            var rtn = d;
            switch (type) {
                case 'G':
                    rtn = d3.format('#,.1f')(d / GB) + 'GB';
                    break;
                case 'M':
                    rtn = d3.format('#,.1f')(d / MB) + 'MB';
                    break;
                case 'K':
                    rtn = d3.format('#,.1f')(d / 1024) + 'KB';
                    break;
                default:
                    rtn = d + 'b';
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
        }
    };
}();
