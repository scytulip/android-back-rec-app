package com.scytulip.nrfUARTbackrec;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Congyin on 9/30/2014.
 */
public class BackRecData {
    public static final String TAG = "nRFUART.BackRecData";
    public static final String strDataFileName = "back_dat.dat";

    private static final int BD_BLOCK_SIZE = 128;                           /**< Size of each pstorage FLASH block (in uint8_t). */
    private static final int BD_BLOCK_COUNT = 256;                          /**< Total No. of pstorage FLASH blocks (256 x 128 = 32K blocks). */
    private static final int BD_DATA_NUM_PER_BLOCK = 120;                   /**< Number of data points per block. */
    private static final int BD_DATA_END_ADDR = BD_DATA_NUM_PER_BLOCK;      /**< End address of data segment in each block. */
    private static final int BD_CONFIG_BASE_ADDR = ((BD_DATA_END_ADDR & 0x3)!=0) ? (((BD_DATA_END_ADDR >> 0x2) + 1) << 0x2):(BD_DATA_END_ADDR);
                                                                            /**< Base address for CONFIG blocks (in uint8_t, aligned to Word). */
    private static final int BD_CONFIG_NUM_PER_BLOCK = 4;                   /**< Number of config info per block (a multiple of 4 bytes). */
    private static final int BD_CONFIG1_OFFSET = 0x0;                       /**< Offset address for CONFIG1 block. */
    private static final int BD_CONFIG2_OFFSET = 0x1;                       /**< Offset address for CONFIG2 block: number of data points. */

    private byte[][] data = new byte[BD_BLOCK_COUNT][BD_BLOCK_SIZE];        /**< Data pool **/

    private int m_cur_data_idx;                                             /**< Current index # for data & config. */
    private int m_cur_block_idx;                                            /**< Current block # of FLASH area for saving current data & config. */

    private Context context;

    BackRecData(Context ctx) {
        clearAll();
        context = ctx;
    }

    /* Clear index and data array */
    public void clearAll() {
        m_cur_data_idx = 0;
        m_cur_block_idx = 0;

        for (int i=0; i<BD_BLOCK_COUNT; i++) {
            for (int j=0; j<BD_BLOCK_SIZE; j++) {
                data[i][j] = (byte)0xFF;
            }
        }
    }

    /* Append received data to current data pool */
    public void appendData(byte[] bdata) {
        if (m_cur_data_idx == BD_BLOCK_SIZE &&
            m_cur_block_idx == BD_BLOCK_COUNT) {
            Log.i(TAG, "Data pool is full. Drop received data:" + bdata.toString());
            return;
        }

        for (int i=0; i<bdata.length; i++) {
            data[m_cur_block_idx][m_cur_data_idx] = bdata[i];

            m_cur_data_idx ++;
            if (m_cur_data_idx == BD_BLOCK_SIZE) {
                m_cur_data_idx = 0;
                m_cur_block_idx ++;
            }
            if (m_cur_block_idx == BD_BLOCK_COUNT) {
                Log.i(TAG, "Data pool is full. Drop received data:" +
                        bdata.toString().substring(i+1));
                break;
            }
        }
    }

    public void writeToBinaryFile() {
        try {
            File fileSDCardDir = Environment.getExternalStorageDirectory();
            File fileSaveFile = new File(fileSDCardDir, "back_data.dat");
            FileOutputStream fout = new FileOutputStream(fileSaveFile);

            for(int i=0; i<BD_BLOCK_COUNT; i++) { fout.write(data[i]); }
            fout.close();

        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

}
