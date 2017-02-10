function bindEvent() {
    $("#q_button").unbind("click").bind("click", function () {
        var data = {};
        var url = "fsanalysis.do";
        data.stockcode = $('#stockcode').val();
        data.beginDate = $('#beginDate').datebox('getValue');
        data.beginTime = $('#beginTime').val();
        data.endDate = $('#endDate').datebox('getValue');
        data.endTime = $('#endTime').val();
        data.period = $('#period').val();
        data.unit = $('#unit').val();
        data.bVolume = $('#bVolume').val();
        data.sVolume = $('#sVolume').val();
        data.zl_volume_all = $('#zl_volume_all').val();
        $.ajax({
                   url: url,// 跳转到 action    
                   data: data,
                   type: 'post',
                   cache: false,
                   dataType: 'json',
                   success: function (data) {
                       draw(data);
                   },
                   error: function () {
                       alert("异常！");
                   }
               });
    })
}
function draw(data) {
    var resList = data.resList;
    var code;
    var catArray = [];
    var seriesArray = [];
    var seriesArray_1 = [];
    $.each(resList, function (i, item) {
        code = item.code;
        catArray.push(item.begin_c + "_" + item.end_c);
        seriesArray.push(item.amount_diff_c);
        seriesArray_1.push(item.amount_var_c);
    });
    Highcharts.chart('stock_draw', {
        chart: {
            type: 'area'
        },
        title: {
            text: '股票资金N流入量'
        },
        xAxis: {
            categories: catArray
        },
        series: [{
            name: code,
            data: seriesArray
        },{
            name:"xyz",
            data:seriesArray_1
        }
        ]
    })
}
                                           