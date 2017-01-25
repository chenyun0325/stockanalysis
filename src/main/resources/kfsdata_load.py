# -*- coding: utf-8 -*-
import json
import logging as log
import sys
import traceback
import tushare as ts


def k_load(code =None,start =None,end=None,ktype='D',retry_count =5,pause=1):
    df = ts.get_hist_data(code=code,start=start,end=end,ktype=ktype,retry_count=retry_count,pause=pause)
    df.insert(0,'code',code)
    df.insert(0,'ktype',ktype)
    return df


def k_load_batch(code_list=None,start=None,end=None,ktype='D'):
    result=[]
    for code in code_list:
        try:
            k_df = k_load(code=code, start=start, end=end, ktype=ktype)
            if (k_df is not None) & (len(k_df.index) > 4):  # 没有数据情况返回
                result.append(res)
        except Exception, e:
            log.error(code)
            log.error(traceback.print_exc())
            log.error(e)
            continue
    return result

if __name__ == '__main__':
    res=k_load_batch(sys.argv[1].split(','),start=sys.argv[2],end=sys.argv[3],ktype=sys.argv[4])
    for df in res:
        for i, row in df.iterrows():
            json_dic = {}
            json_dic['code'] = row['code']
            json_dic['date'] = i
            json_dic['ktype'] = row['ktype']
            json_dic['open'] = row['open']
            json_dic['high'] = row['high']
            json_dic['close'] = row['close']
            json_dic['low'] = row['low']
            json_dic['volume'] = row['volume']
            json_dic['price_change'] = row['price_change']
            json_dic['p_change'] = row['p_change']
            json_dic['ma5'] = row['ma5']
            json_dic['ma10'] = row['ma10']
            json_dic['ma20'] = row['ma20']
            json_dic['v_ma5'] = row['v_ma5']
            json_dic['v_ma10'] = row['v_ma10']
            json_dic['v_ma20'] = row['v_ma20']
            json_dic['turnover'] = row['turnover']
            json_str=json.dumps(json_dic)
            print json_str



