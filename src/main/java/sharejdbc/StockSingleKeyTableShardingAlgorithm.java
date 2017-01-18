package sharejdbc;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.zip.CRC32;

/**
 * Created by cy111966 on 2017/1/15.
 */
public class StockSingleKeyTableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<String>{


    int tableSize =40;
    @Override
    public String doEqualSharding(Collection<String> tableNames,
                                  ShardingValue<String> shardingValue) {
      String value = shardingValue.getValue();
      int vHash = newCompatHashingAlg(value);
      System.err.println(vHash);
      System.err.println(vHash % tableSize);
      for (String tableName : tableNames) {
        if (tableName.endsWith(vHash % tableSize + "")) {
          System.err.println(tableName);
          return tableName;
        }
      }
      throw new IllegalArgumentException();
    }

    @Override
    public Collection<String> doInSharding(Collection<String> tableNames,
                                           ShardingValue<String> shardingValue) {
      Collection<String> tablesRes = new HashSet<>();
      Collection<String> values = shardingValue.getValues();
      for (String value : values) {
        int vHash = newCompatHashingAlg(value);
        for (String tableName : tableNames) {
          if (tableName.endsWith(vHash % tableSize + "")) {
            tablesRes.add(tableName);
          }
        }
      }
      return tablesRes;
    }

    @Override
    public Collection<String> doBetweenSharding(Collection<String> tableNames,
                                                ShardingValue<String> shardingValue) {
      return null;
    }
    private static int newCompatHashingAlg( String key ) {
      CRC32 checksum = new CRC32();
      checksum.update( key.getBytes() );
      int crc = (int) checksum.getValue();
      return (crc >> 16) & 0x7fff;
    }
}
