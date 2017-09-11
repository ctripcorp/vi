(function() {
    'use strict';

    function CodeModalController($scope, configService, $uibModalInstance, toastr, metaInfo, codeService) {
        $scope.conditions = [{
            name: '',
            template: true
        }];
        var delItem = '[delete current]';
        var names = [];
        var meta = {};
        metaInfo.traceId = VIUtil.uuid();
        $scope.metaInfo = metaInfo;
        $scope.winIsOpen = true;
        $scope.vm = {};
        var vm = $scope.vm;
        vm.formatDesc = VIUtil.formatDesc;
        vm.isDefaultUI = metaInfo.isDefaultUI;


        function caculateCurrentFields() {
            var fullName = (vm.conditionFormula || '').match(/[^\s>=<\&\|]*$/i)[0];
            var parts = fullName.split('.');
            var popCount = intelligencePath.length - parts.length;
            if (popCount > 0) {
                intelligencePath.pop();
                var desc = intelligencePath[intelligencePath.length - 1];
                if (desc === '') {
                    vm.currentFields = metaInfo.meta;
                } else {
                    vm.currentFields = $scope.getFieldByDesc(desc);
                }
            }

        }

        var intelligencePath = [''];

        $scope.seleField = function(item) {

            vm.conditionFormula = vm.conditionFormula.substr(0, vm.conditionFormula.length - vm.currentWord.length) + item.name;

        };

        $scope.onKeyDown = function(event) {
            $scope.winIsOpen = true;
            vm.enter = false;
            vm.currentWord = (vm.conditionFormula || '').match(/[a-z_$0-9]*$/i)[0];
            var lastchar = (vm.conditionFormula || '').slice(-1);
            if (vm.currentWord === '' && event.keyCode != 190 && event.keyCode != 13 && event.keyCode != 40 && event.keyCode != 38) {
                vm.currentWord = '';
                vm.currentFields = metaInfo.meta;
                intelligencePath = [''];
                return;

            }
            vm.isDot = (lastchar == '.');
            if (lastchar === '>') return;
            if ($scope.winIsOpen) {
                var listLen = vm.currentFields.length;
                switch (event.keyCode) {

                    case 8: //delete
                        caculateCurrentFields();
                        break;
                    case 32: //blank
                        vm.currentWord = '';
                        vm.currentFields = metaInfo.meta;
                        intelligencePath = [''];
                        break;
                    case 190: //dot
                        var currentWord = vm.conditionFormula.match(/[a-z_$0-9]*\.$/i)[0];
                        currentWord = currentWord.substr(0, currentWord.length - 1);
                        for (var i = 0; i < vm.currentFields.length; i++) {

                            var item = vm.currentFields[i];
                            if (item.name == currentWord) {
                                vm.currentWord = '';
                                $scope.selectedIndex = 0;
                                if (item.desc in CLASSFIELDMAP) {
                                    vm.currentFields = CLASSFIELDMAP[item.desc];
                                    intelligencePath.push(item.desc);
                                } else {
                                    vm.currentFields = [];
                                    updateClassFieldMap(item.desc, function(d) {
                                        vm.currentFields = d;
                                        intelligencePath.push(item.desc);
                                    });

                                }

                                break;
                            }
                        }
                        //$scope.currentWord = 
                        break;
                    case 13: //enter
                        vm.enter = true;
                        var seleItem = vm.list[$scope.selectedIndex];

                        if (seleItem && vm.conditionFormula) {
				$scope.seleField(seleItem);
                        }
                        break;
                    case 38: //up

                        vm.isDot = true;
                        if ($scope.selectedIndex) {

                            $scope.selectedIndex--;

                            if ($scope.selectedIndex <= 0)
                                $scope.selectedIndex = 0;

                        } else {
                            $scope.selectedIndex = listLen - 1;
                        }
                        break;
                    case 40: //down
                        vm.isDot = true;
                        if ($scope.selectedIndex >= 0) {

                            $scope.selectedIndex++;

                            if ($scope.selectedIndex >= listLen)
                                $scope.selectedIndex = 0;

                        } else {
                            $scope.selectedIndex = 0;
                        }
                        break;

                }
            }
        };

        for (var n in metaInfo.meta) {
            var item = metaInfo.meta[n];
            names.push(item.name);
            meta[item.name] = item.desc;

        }
        $scope.names = names;
        vm.currentFields = metaInfo.meta;
        var ops = {
            'primitive': ['>', '>=', '<', '<=', '==', '!='],
            'boolean': ['IS TRUE', 'IS FALSE'],
            'string': ['IS NULL', 'IS NOT NULL', 'EQ', 'NOT EQ', 'LEN >', 'LEN >=', 'LEN <', 'LEN <=', 'LEN ==', 'LEN !='],
            'object': ['IS NULL', 'IS NOT NULL']
        };
        $scope.ops = ops;
        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

        var Opcodes = {
            STRLEN: -2,
            STREQUAL: -3,

            LCMP: 148, // -
            FCMPL: 149, // -
            FCMPG: 150, // -
            DCMPL: 151, // -
            DCMPG: 152, // -
            IFEQ: 153,
            IFNE: 154,
            IFLT: 155, // -
            IFGE: 156, // -
            IFGT: 157, // -
            IFLE: 158, // -
            IF_ICMPEQ: 159, // -
            IF_ICMPNE: 160, // -
            IF_ICMPLT: 161, // -
            IF_ICMPGE: 162, // -
            IF_ICMPGT: 163, // -
            IF_ICMPLE: 164, // -
            IF_ACMPEQ: 165, // -
            IF_ACMPNE: 166, // -
            GETFIELD: 180,

            IFNULL: 198,
            IFNONULL: 199,


        };

        function setStrOpsAndCmpOps(c, a) {

            var mustBeNumber = false;
            switch (c.op) {
                case ops.string[0]:
                    a.opcode = -1;
                    a.cmpcode = Opcodes.IFNONULL;
                    break;
                case ops.string[1]:
                    a.opcode = -1;
                    a.cmpcode = Opcodes.IFNULL;
                    break;
                case ops.string[2]:
                    a.opcode = Opcodes.STREQUAL;
                    a.cmpcode = Opcodes.IFEQ;
                    break;
                case ops.string[3]:
                    a.opcode = Opcodes.STREQUAL;
                    a.cmpcode = Opcodes.IFNE;
                    break;
                case ops.string[4]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPLE;
                    mustBeNumber = true;
                    break;
                case ops.string[5]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPLT;
                    mustBeNumber = true;
                    break;
                case ops.string[6]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPGE;
                    mustBeNumber = true;
                    break;
                case ops.string[7]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPGT;
                    mustBeNumber = true;
                    break;
                case ops.string[8]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPNE;
                    mustBeNumber = true;
                    break;
                case ops.string[9]:
                    a.opcode = Opcodes.STRLEN;
                    a.cmpcode = Opcodes.IF_ICMPEQ;
                    mustBeNumber = true;
                    break;

            }
            if (mustBeNumber && isNaN(c.value)) {
                c.error = 'The field must be a number';
            }

        }

        var primitiveOpCodes = {

            '>': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPL,
                'D': Opcodes.DCMPL,
                'J': Opcodes.LCMP,

            },
            '>=': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPL,
                'D': Opcodes.DCMPL,
                'J': Opcodes.LCMP,

            },
            '<': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPG,
                'D': Opcodes.DCMPG,
                'J': Opcodes.LCMP,

            },
            '<=': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPG,
                'D': Opcodes.DCMPG,
                'J': Opcodes.LCMP,

            },
            '==': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPL,
                'D': Opcodes.DCMPL,
                'J': Opcodes.LCMP,

            },
            '!=': {
                'I': -1,
                'S': -1,
                'B': -1,
                'C': -1,
                'F': Opcodes.FCMPL,
                'D': Opcodes.DCMPL,
                'J': Opcodes.LCMP,

            }
        };
        var primitiveCmpCodes = {

            '>': {
                'I': Opcodes.IF_ICMPLE,
                'S': Opcodes.IF_ICMPLE,
                'B': Opcodes.IF_ICMPLE,
                'C': Opcodes.IF_ICMPLE,
                'F': Opcodes.IFLE,
                'D': Opcodes.IFLE,
                'J': Opcodes.IFLE,

            },
            '>=': {
                'I': Opcodes.IF_ICMPLT,
                'S': Opcodes.IF_ICMPLT,
                'B': Opcodes.IF_ICMPLT,
                'C': Opcodes.IF_ICMPLT,
                'F': Opcodes.IFLT,
                'D': Opcodes.IFLT,
                'J': Opcodes.IFLT,


            },
            '<': {
                'I': Opcodes.IF_ICMPGE,
                'S': Opcodes.IF_ICMPGE,
                'B': Opcodes.IF_ICMPGE,
                'C': Opcodes.IF_ICMPGE,
                'F': Opcodes.IFGE,
                'D': Opcodes.IFGE,
                'J': Opcodes.IFGE,

            },
            '<=': {
                'I': Opcodes.IF_ICMPGT,
                'S': Opcodes.IF_ICMPGT,
                'B': Opcodes.IF_ICMPGT,
                'C': Opcodes.IF_ICMPGT,
                'F': Opcodes.IFGT,
                'D': Opcodes.IFGT,
                'J': Opcodes.IFGT,

            },
            '==': {
                'I': Opcodes.IF_ICMPNE,
                'S': Opcodes.IF_ICMPNE,
                'B': Opcodes.IF_ICMPNE,
                'C': Opcodes.IF_ICMPNE,
                'F': Opcodes.IFNE,
                'D': Opcodes.IFNE,
                'J': Opcodes.IFNE,

            },
            '!=': {
                'I': Opcodes.IF_ICMPEQ,
                'S': Opcodes.IF_ICMPEQ,
                'B': Opcodes.IF_ICMPEQ,
                'C': Opcodes.IF_ICMPEQ,
                'F': Opcodes.IFEQ,
                'D': Opcodes.IFEQ,
                'J': Opcodes.IFEQ,

            }
        };

        function setPrimitiveOpsAndCmpOps(c, a) {

            var desc = c.desc;
            if (c.fields.length > 0) {
                desc = c.fields[c.fields.length - 1].desc;
            }
            a.opcode = primitiveOpCodes[c.op][desc];
            a.cmpcode = primitiveCmpCodes[c.op][desc];

        }
        var conditionFormula;
        //$scope.isDefaultUI = false;

        var getASMConditions = function(conditions) {
            conditionFormula = '';

            var rtn = [];
            var len = conditions.length;
            var i;
            for (i = 0; i < len; i++) {
                var item = conditions[i];
                delete item.error;
                if (item.name === '') {
                    break;
                }
                var asmCondition = {
                    'fieldName': item.name
                };
                asmCondition.classFields = item.fields;
                if (!item.op) {

                    item.error = 'You must specify the type of operation';
                    break;
                }

                switch (item.type) {

                    case 'string':
                        setStrOpsAndCmpOps(item, asmCondition);
                        asmCondition.value = item.value;
                        break;
                    case 'primitive':
                        setPrimitiveOpsAndCmpOps(item, asmCondition);
                        if (isNaN(item.value)) {
                            item.error = 'The field must be a number';
                        } else {
                            asmCondition.value = item.value;
                        }
                        break;
                    case 'object':
                        asmCondition.opcode = -1;
                        switch (item.op) {
                            case ops.object[0]:
                                asmCondition.cmpcode = Opcodes.IFNONULL;
                                break;
                            case ops.object[1]:
                                asmCondition.cmpcode = Opcodes.IFNULL;
                                break;

                        }
                        break;
                    case 'boolean':
                        asmCondition.opcode = -1;
                        asmCondition.cmpcode = Opcodes.IF_ICMPNE;
                        switch (item.op) {
                            case ops.boolean[0]:
                                asmCondition.value = 1;
                                break;
                            case ops.boolean[1]:
                                asmCondition.value = 0;
                                break;

                        }
                        break;

                }

                asmCondition.desc = meta[item.name];

                var varName = item.name;

                if (item.fields.length > 0) {

                    varName += '.' + item.fields.map(function(x) {
                        return x.name;

                    }).join('.');
                }

                conditionFormula += varName +
                    ' ' + item.op + ' ' + (item.value ? item.value + ' ' : '') + '&& ';
                rtn.push(asmCondition);
            }

            if (conditionFormula.length > 4) {
                conditionFormula = conditionFormula.substr(0, conditionFormula.length - 4);
            }

            return rtn;

        };

        /*
        $scope.getFieldByDesc = function(desc) {


        };
	*/

        var SYSCLASS = {
            'String': 0,
            'Long': 0,
            'Double': 0,
            'Float': 0,
            'Byte': 0,
            'Integer': 0,
            'Character': 0,
            'Short': 0,
            'Boolean': 0
        };

        var JAVAKEYWORDS = {
            'abstract': 0,
            'assert': 0,
            'boolean': 0,
            'break': 0,
            'byte': 0,
            'case': 0,
            'catch': 0,
            'char': 0,
            'class': 0,
            'const': 0,
            'continue': 0,
            'default': 0,
            'do': 0,
            'double': 0,
            'else': 0,
            'enum': 0,
            'extends': 0,
            'final': 0,
            'finally': 0,
            'float': 0,
            'for': 0,
            'goto': 0,
            'if': 0,
            'implements': 0,
            'import': 0,
            'instanceof': 0,
            'int': 0,
            'interface': 0,
            'long': 0,
            'native': 0,
            'new': 0,
            'package': 0,
            'private': 0,
            'protected': 0,
            'public': 0,
            'return': 0,
            'short': 0,
            'static': 0,
            'strictfp': 0,
            'super': 0,
            'switch': 0,
            'synchronized': 0,
            //'this': 0,
            'throw': 0,
            'throws': 0,
            'transient': 0,
            'try': 0,
            'void': 0,
            'volatile': 0,
            'while': 0,
            'true': 0,
            'false': 0,
            'null': 0
        };

        function formatCondition(c) {

            return c.replace(/(^|[^a-z_$0-9\.])([_a-z$][a-z_$0-9]*)/gi, function(match, m1, m2) {

                if (m2 in SYSCLASS) {
                    return m1 + 'java.lang.' + m2;

                } else if (m2 in JAVAKEYWORDS) {

                    return m1 + m2;
                } else {
                    return m1 + 'localVariables[' + m2 + ']';
                }
            });
        }


        $scope.ok = function() {

            var conditions = vm.conditionFormula;

            var formular = conditions;
            if (vm.isDefaultUI) {
                var asmConditions = getASMConditions($scope.conditions);
                for (var i = 0; i < $scope.conditions.length; i++) {

                    if ($scope.conditions[i].error) {

                        return;
                    }
                }
                if (asmConditions.length === 0) {
                    toastr.warning('no condition found!');

                    return;
                }
                conditions = {
                    'd': asmConditions
                };
                formular = conditionFormula;

            } else {
                var expect;
                var tmpC = [];
                var result = [];
                var isInStr = false;
                for (var i = 0; i < conditions.length; i++) {
                    var c = conditions[i];
                    var isChange = false;
                    if (!expect) {
                        switch (c) {
                            case '"':
                                expect = '"';
                                isInStr = true;
                                isChange = true;
                                break;
                            case "'":
                                expect = "'";
                                isInStr = true;
                                isChange = true;
                                break;
                            default:
                                break;

                        }
                    } else {
                        if (expect === c) {
                            expect = null;
                            isChange = true;
                            isInStr = false;
                        }
                    }

                    tmpC.push(c);
                    if (!isInStr) {
                        if (isChange) {
                            result.push(tmpC.join(''));
                            tmpC = [];
                        }
                    } else {
                        if (isChange) {
                            result.push(formatCondition(tmpC.join('')));
                            tmpC = [];

                        }

                    }

                }
                conditions = result.join('') + formatCondition(tmpC.join(''));

            }
            //console.log(conditions);
            codeService.registerBreakpoint({
                'source': metaInfo.fullName,
                'line': metaInfo.lineNum,
                'conditions': conditions,
                'breakpointId': metaInfo.traceId
            }, function(d) {
                $uibModalInstance.close({
                    'formular': formular,
                    'result': d
                });
            });


        };

        var NOVALUEOPS = ['IS NULL', 'IS NOT NULL', 'IS TRUE', 'IS FALSE'];

        $scope.isNeedValue = function(item) {

            if (!item.op) {
                return false;
            } else if (NOVALUEOPS.indexOf(item.op) >= 0) {

                return false;
            } else {

                return true;
            }

        };


        var getItemByDesc = function(desc) {
            if (desc == 'Z') {
                return 'boolean';
            } else if (desc.length == 1) {
                return 'primitive';
            } else if (desc == 'Ljava/lang/String;') {
                return 'string';
            } else {
                return 'object';
            }

        };

        var CLASSFIELDMAP = {};
        var SKIPDESC = {
            'Ljava/lang/String;': 0,
            'Ljava/lang/Object;': 0
        };
        var updateClassFieldMap = function(desc, callback) {

            if (desc in CLASSFIELDMAP || desc in SKIPDESC) {

                return;
            } else {
                codeService.getClassFields({
                    'className': desc.substr(1, desc.length - 2)
                }, function(data) {
                    if (data instanceof Array && data.length > 0) {
                        CLASSFIELDMAP[desc] = data;
                        if (callback) {
                            callback(data);
                        }
                    }

                });

            }

        };

        $scope.getCurrentField = function(i, c) {
            if (i === 0) {
                return $scope.getFieldByDesc(c.desc);
            } else {
                return $scope.getFieldByDesc(c.fields[i - 1].desc);

            }

        };

        $scope.updateField = function(c, f, i) {

            var desc;
            if (f) {
                desc = f.desc;
                if (c.fields[i].name != f.name) {
                    c.fields[i] = f;
                    c.fields.splice(i + 1);
                }
            } else {
                if (i === 0) {
                    desc = c.desc;
                    c.fields = [];
                } else {
                    desc = c.fields[i - 1];
                    c.fields.splice(i);

                }


            }

            c.type = getItemByDesc(desc);
            updateClassFieldMap(desc);

        };

        $scope.getFieldByDesc = function(desc) {

            return CLASSFIELDMAP[desc];
        };

        $scope.hasDescFields = function(desc) {

            return desc in CLASSFIELDMAP;
        };

        $scope.pushField = function(c, f) {
            c.fields.push(f);
            updateClassFieldMap(f.desc);
            c.type = getItemByDesc(f.desc);

        };

        $scope.deleteCondition = function(c) {

            VIUtil.removeArrayItem($scope.conditions, c);
        };
        $scope.nameChange = function(c, n) {

            c.name = n;

            updateClassFieldMap(meta[n]);
            if (c.template && c.name.length > 0) {
                var newItem = angular.copy(c);
                delete newItem.template;
                c.name = '';
                $scope.conditions.pop();
                newItem.desc = meta[n];
                newItem.type = getItemByDesc(newItem.desc);
                newItem.fields = [];
                $scope.conditions.push(newItem);
                $scope.conditions.push(c);
            } else {
                c.desc = meta[n];
                c.fields = [];
                c.type = getItemByDesc(c.desc);

            }



        };

    }

    angular
        .module('viModule')
        .controller('CodeContentController',
            function($element, $compile, $scope, $timeout, codeService, $stateParams, $cookies, $window, toastr, $rootScope, $uibModal, ScrollbarService, $confirm) {
                var vm = this;
                vm.debugInfo = {};
                vm.rootPath = "#code/folder/";
                vm.enableCondition = true;
                vm.isTab = false;
                var params = $element.attr('params');
                var codeContentScrollbar;
                if (params) {
                    $stateParams = VIUtil.queryToObj(params);
                    vm.isTab = true;
                    if ($stateParams.line !== '') {
                        vm.jumpLine = $stateParams.line;
                    }
                }

                codeService.isDefaultDebugger(function(d) {
                    vm.isDefaultUI = d;
                });


                var currentPath = $stateParams.path;
                var lastJumpLine;
                vm.panelWidth = ($element.prop('clientWidth') - 40) + 'px';
                vm.panelMaxHeight = '100px';
                vm.maxHeight = ($window.innerHeight - 190) + 'px';
                vm.jcontentWidth = ($window.innerWidth * 0.9 * 0.7) + 'px';

                var jumpToLine = function() {
                    if (lastJumpLine) {

                        lastJumpLine.className = lastJumpLine.className.replace(' focus', '');
                    }
                    var seleLine = ($element[0].querySelector('.line:nth-child(' + vm.jumpLine + ')'));
                    if (seleLine) {
                        codeContentScrollbar.update();
                        seleLine.className += ' focus';
                        lastJumpLine = seleLine;
                        var height = seleLine.offsetTop;
                        if (height > 180) {
                            height -= 180;
                        }
                        codeContentScrollbar.scrollTo(0, height);
                    } else {
                        toastr.warning('out of range');
                    }

                };

                vm.onGOTOKeyDown = function(event) {

                    switch (event.keyCode) {
                        case 13:
                            if (!(/\d+/.test(vm.jumpLine))) {

                                toastr.warning('must be an integer!');
                                return;
                            }
                            jumpToLine();
                            break;

                    }

                };


                var resizeFun = function() {
                    $scope.$apply(function() {
                        vm.maxHeight = ($window.innerHeight - 190) + 'px';
                        vm.jcontentWidth = ($window.innerWidth * 0.9 * 0.7) + 'px';
                    });
                };

                var stopDebug = function() {

                    if (vm.traceId && vm.traceId.length > 0 && vm.seleLine) {
                        codeService.stopDebug(vm.traceId, function(d) {

                            vm.showDebugPanel = false;
                            vm.hasResult = false;
                            d3.select(vm.seleLine).attr('selected', null);
                            vm.seleLine = null;
                        });
                    }
                };

                angular.element($window).on('resize', resizeFun);
                $scope.$on('$destroy', function() {
                    angular.element($window).off('resize', resizeFun);
                    vm.showDebugPanel = false;
                    stopDebug();
                });

                vm.gotoSettings = function() {
                    $rootScope.$broadcast('$newViView', 'Settings', {}, 'Settings');
                };

                vm.panelMin = function() {
                    var clientHeight = '100px';
                    vm.panelMaxHeight = clientHeight;

                };
                vm.panelMax = function() {
                    var clientHeight = ($window.innerHeight - 100) + 'px';
                    vm.panelMaxHeight = clientHeight;

                };
                var pathParts = currentPath.split('|');
                var rawPrjPath = pathParts.shift();
                var folderPath = pathParts.join('/');
                var prjId = pathParts.shift();
                if (rawPrjPath == 'jar') {
                    vm.prjFullPath = prjId;
                    vm.rootPath = "#code/folder/jar|" + prjId;
                } else {
                    vm.prjFullPath = rawPrjPath.replace(/\$@/g, '/');
                }
                pathParts.pop();
                vm.pathParts = pathParts;
                vm.getUrl = function(index) {
                    var tmp = vm.pathParts.slice(0, index + 1);
                    return rawPrjPath + '|' + prjId + '|' + tmp.join('|');

                };


                vm.formatTrace = function(trace) {
                    var methodDetail = 'Native method';
                    if (trace.lineNumber > 0) {
                        var ns = trace.declaringClass.substr(0, trace.declaringClass.lastIndexOf('.'));
                        methodDetail = '<span jns="' + ns + '" ng-click="vm.viewSource($event)" class="link">' + trace.fileName + ':' + trace.lineNumber + '</span>';
                    }
                    return trace.declaringClass + '.' + trace.methodName + '(' + methodDetail + ')';
                };

                vm.viewSource = function(event) {

                    var $this = angular.element(event.currentTarget);
                    var ns = $this.attr('jns');
                    var name = $this.text();
                    codeService.viewCode(ns, name);
                };



                function monitorDebugInfo() {

                    codeService.getCapturedFrame(vm.traceId, function(data) {

                        if (vm.seleLine === null) {
                            return;
                        }
                        if (data instanceof VIException) {
                            toastr.error('Message:' + data.Message + '\nType:' + data.Type, 'get captured failed!', {
                                'timeOut': 100000
                            });
                            return;
                        }
                        if (data && data.locals) {
                            vm.debugInfo = data;
                            vm.hasResult = true;
                        } else if (vm.showDebugPanel) {
                            $timeout(monitorDebugInfo, 2000);

                        }
                    });

                }

                var bindLineClickEve = function() {

                    d3.select($element[0]).selectAll('div.line>.line-number').on('click', function() {

                        if (!vm.canDebug) {
                            return;
                        }

                        /*
			if(vm.isDefaultUI){

				toastr.error('no debugger found!');
				return;
			}
			*/

                        var parentEle = d3.select(this.parentElement);
                        var numTxt = this.innerText;
                        var lineTxt = this.parentElement.innerText;
                        var realLineTxt = lineTxt.substr(numTxt.length);
                        var $this = this;
                        if ((vm.formular || !vm.enableCondition) && vm.showDebugPanel) {
                            var cfm = $confirm({
				    text: 'Current debug condition is [' +(vm.enableCondition?vm.formular:'traceId='+vm.traceId) + ']' + (vm.hasResult ? '.' : ', do you want stop debug?')
                            }).then(function() {

                                stopDebug();

                            });

                            return;
                        }

                        vm.hasResult = false;
                        if ((/^\s*[a-zA-Z]{1,}/g).test(realLineTxt)) {

                            if (vm.seleLine && $this.parentElement != vm.seleLine) {

                                d3.select(vm.seleLine).attr('selected', null);
                                vm.seleLine = null;
                            }
                            if (!parentEle.attr('selected')) {
                                if (vm.traceId && vm.traceId.length > 2 && !vm.enableCondition) {
                                    vm.hasResult = false;
				    vm.formular = '';
                                    codeService.registerBreakpoint({
                                        'source': vm.fullName,
                                        'line': numTxt,
                                        'breakpointId': vm.traceId
                                    }, function(result) {

                                        vm.showDebugPanel = false;
                                        parentEle.attr('selected', null);
                                        vm.seleLine = null;
                                        if (result instanceof VIException) {
                                            toastr.error('Message:' + result.Message + '\nType:' + result.Type);
                                            return;
                                        }
                                        if (result) {

                                            vm.debugInfo = {};
                                            vm.hasResult = false;
                                            vm.showDebugPanel = true;
                                            monitorDebugInfo();
                                            parentEle.attr('selected', true);
                                            vm.seleLine = $this.parentElement;
                                        } else {
                                            toastr.warning('register breakpoint failed! cannot set breakpoint in here!');
                                        }
                                    });
                                } else if (vm.enableCondition) {
                                    //numTxt = '56';
                                    codeService.getLineVars({
                                        'className': vm.fullName,
                                        'lineNum': numTxt
                                    }, function(result) {
                                        parentEle.attr('selected', null);
                                        vm.seleLine = null;
                                        if (result instanceof VIException) {
                                            toastr.error('Message:' + result.Message + '\nType:' + result.Type);
                                            return;
                                        }
                                        parentEle.attr('selected', null);
                                        parentEle.attr('selected', true);
                                        vm.seleLine = $this.parentElement;
                                        var metaInfo = {
                                            meta: result,
                                            fullName: vm.fullName,
                                            lineNum: numTxt,
                                            traceId: vm.traceId,
                                            isDefaultUI: vm.isDefaultUI
                                        };

                                        var modalInstance = $uibModal.open({
                                            animation: true,
                                            templateUrl: 'codeContent.html',
                                            controller: CodeModalController,
                                            size: 'lg',
                                            resolve: {
                                                'metaInfo': metaInfo
                                            }
                                        });
                                        modalInstance.closed.then(function() {
                                            if (!vm.showDebugPanel) {
                                                parentEle.attr('selected', null);
                                                vm.seleLine = null;

                                            }

                                        });
                                        modalInstance.result.then(function(d) {

                                            vm.hasResult = false;
                                            vm.showDebugPanel = false;
                                            parentEle.attr('selected', null);
                                            vm.seleLine = null;
                                            vm.formular = d.formular;
                                            if (d.result instanceof VIException) {
                                                toastr.error('Message:' + d.result.Message + '\nType:' + d.result.Type);
                                                return;
                                            }
                                            if (d.result) {

                                                vm.traceId = metaInfo.traceId;
                                                vm.debugInfo = {};
                                                vm.showDebugPanel = true;
                                                monitorDebugInfo();
                                                parentEle.attr('selected', true);
                                                vm.seleLine = $this.parentElement;
                                            } else {
                                                toastr.warning('register breakpoint failed! cannot set breakpoint in here!');
                                            }
                                        });

                                    });
                                    return;

                                } else {
                                    toastr.warning('you must set trace id first!', 'warning');

                                    return;
                                }
                            } else {
                                if (vm.seleLine) {
                                    stopDebug();
                                }
                            }

                            if (!vm.seleLine) {

                                $scope.$apply(function() {
                                    vm.showDebugPanel = false;
                                });
                                //monitorDebugInfo(false);
                            } else if (!vm.showDebugPanel) {
                                $scope.$apply(function() {
                                    vm.showDebugPanel = true;
                                });
                            }
                        } else {

                            toastr.warning('you can not set a monitoring point here!', 'warning');
                        }
                    });
                };

                //bindLineClickEve();


                codeService.getContent(folderPath, function(rtn) {
                    if (rtn.data || rtn.file_name) {

                        vm.raw = rtn.data ? decodeURIComponent(escape(atob(unescape(encodeURIComponent(rtn.data.content))))) : rtn.content;
                        vm.name = rtn.data ? rtn.data.file_name : rtn.file_name;
                        vm.raw = VIUtil.escapeHTML(vm.raw);
                        var size = rtn.data ? rtn.data.size : rtn.size;
                        vm.isJava = (/.*\.java$/i).test(vm.name);
                        if (vm.isJava) {
                            var packgeNameMatch = vm.raw.match(/\s*package\s+([^;]+)\s*;/);
                            if (packgeNameMatch && packgeNameMatch.length == 2) {
                                vm.fullName = packgeNameMatch[1].replace(/\./g, '/') + '/' + vm.name;
                            }
                        }
                        vm.size = VIUtil.formatBytes(size);
                        vm.canDebug = vm.isJava && ($cookies.get('allowDebug') == 'true');

                        $scope.$on('$allowDebugChange', function() {
                            vm.canDebug = vm.isJava && ($cookies.get('allowDebug') == 'true');
                            if (vm.canDebug) {
                                bindLineClickEve();

                            }
                        });


                        var renderCode = function(viewname) {
                            var ele = $element[0].querySelector('div.scrollbar');
                            if (ele) {
                                codeContentScrollbar = Scrollbar.init(ele);
                            } else {

                                $timeout(renderCode, 500);
                            }

                            var blocks = $element[0].querySelector('.scrollbar pre code');
                            angular.element(blocks).html(vm.raw);

                            if (blocks) {
                                hljs.highlightBlock(blocks);

                                if (vm.isJava && typeof(vm.jumpLine) != 'undefined') {
                                    jumpToLine();
                                }
                                if (vm.canDebug) {
                                    bindLineClickEve();

                                }

                            }
                        };
                        $scope.$watch('$viewContentLoaded', renderCode);


                    }

                });



            });
})();
