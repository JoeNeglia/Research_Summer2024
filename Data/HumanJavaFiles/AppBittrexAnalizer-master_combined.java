package br.com.bittrexanalizer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.paulinho.bittrexanalizer", appContext.getPackageName());
    }
}


package br.com.bittrexanalizer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package br.com.bittrexanalizer.database.bd;

import android.util.Log;

/**
 * Created by PauLinHo on 24/06/2017.
 */

public class ScriptCreateTable {


    public static String criarTabelaTICKER() {


        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE TICKER( ");
        sql.append("_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sql.append("nomeExchange varchar(45) not null unique, ");
        sql.append("sigla varchar(45) not null unique, ");
        sql.append("urlApi varchar(45) not null unique, ");
        sql.append("last decimal(10,8) not null , ");
        sql.append("bid decimal(10,8) not null , ");
        sql.append("ask decimal(10,8) not null , ");
        sql.append("isBought boolean not null , ");
        sql.append("avisoBuyInferior decimal(10,8) , ");
        sql.append("avisoBuySuperior decimal(10,8) , ");
        sql.append("avisoStopLoss decimal(10,8) , ");
        sql.append("avisoStopGain decimal(10,8), ");
        sql.append("valorDeCompra decimal(10,8)) ");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String criarTabelaORDER() {

        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE ORDER( ");
        sql.append("_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sql.append("uuid varchar(45) not null unique, ");
        sql.append("orderUiid varchar(45) not null unique, ");
        sql.append("exchange varchar(45), ");
        sql.append("orderType varchar(45), ");
        sql.append("quantity decimal(10,8), ");
        sql.append("quantityRemaining decimal(10,8), ");
        sql.append("limit decimal(10,8), ");
        sql.append("comissionPaid decimal(10,8), ");
        sql.append("price decimal(10,8), ");
        sql.append("pricePerUnit decimal(10,8), ");
        sql.append("opened date, ");
        sql.append("closed date, ");
        sql.append("cancelInitiated String, ");
        sql.append("immediateOrCancel String, ");
        sql.append("isConditional String, ");
        sql.append("isConditional String, ");
        sql.append("condition String, ");
        sql.append("conditionTarget String) ");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String criarTabelaCONFIGURACAO() {


        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE CONFIGURACAO( ");
        sql.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sql.append("propriedade varchar(45) not null unique, ");
        sql.append("valor varchar(45)) ");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String criarTabelaAPICREDENTIALS() {


        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE API_CREDENTIALS( ");
        sql.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sql.append("key varchar(45) not null unique, ");
        sql.append("secret varchar(45) not null unique) ");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }



}



package br.com.bittrexanalizer.database.bd;

import android.util.Log;

/**
 * Created by PauLinHo on 24/06/2017.
 */

public class ScriptDropTable {


    public static String excluirTabelaTICKER() {

        StringBuffer sql = new StringBuffer();
        sql.append("DROP TABLE IF EXISTS TICKER");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String excluirTabelaCONFIGURACAO() {

        StringBuffer sql = new StringBuffer();
        sql.append("DROP TABLE IF EXISTS CONFIGURACAO");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String excluirTabelaAPICREDENTIALS() {

        StringBuffer sql = new StringBuffer();
        sql.append("DROP TABLE IF EXISTS API_CREDENTIALS");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

    public static String excluirTabelaORDER() {

        StringBuffer sql = new StringBuffer();
        sql.append("DROP TABLE IF EXISTS ORDER");

        Log.i("BITTREX", sql.toString());

        return sql.toString();
    }

}




package br.com.bittrexanalizer.database.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by PauLinHo on 24/06/2017.
 */

/**
 * Classe que representa o BD do projeto
 */
public class BittrexBD extends SQLiteOpenHelper {

    private static Integer VERSION_BD = 8;

    public BittrexBD(Context context){

        super(context, "bittrexBD",null, VERSION_BD);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(ScriptDropTable.excluirTabelaTICKER());
        db.execSQL(ScriptDropTable.excluirTabelaCONFIGURACAO());
        db.execSQL(ScriptDropTable.excluirTabelaAPICREDENTIALS());

        db.execSQL(ScriptCreateTable.criarTabelaTICKER());
        db.execSQL(ScriptCreateTable.criarTabelaCONFIGURACAO());
        db.execSQL(ScriptCreateTable.criarTabelaAPICREDENTIALS());

        Log.i("BITTREX", "Connection create with success in onCREATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(ScriptDropTable.excluirTabelaTICKER());
        db.execSQL(ScriptDropTable.excluirTabelaCONFIGURACAO());
        db.execSQL(ScriptDropTable.excluirTabelaAPICREDENTIALS());

        db.execSQL(ScriptCreateTable.criarTabelaTICKER());
        db.execSQL(ScriptCreateTable.criarTabelaCONFIGURACAO());
        db.execSQL(ScriptCreateTable.criarTabelaAPICREDENTIALS());

        Log.i("BITTREX", "Connection create with success in onUPDATE");
    }
}


package br.com.bittrexanalizer.database.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by PauLinHo on 24/06/2017.
 */

/**
 * Retonar uma connection válida do BD
 */
public class ConnectionFactory {

    public static SQLiteDatabase getConnection(Context context){
        BittrexBD bittrexBD = new BittrexBD(context);
        SQLiteDatabase conn = null;

        try{
            conn = bittrexBD.getWritableDatabase();
        }catch (Exception e){
            System.out.print(e.getMessage());
            Log.i("BITTREX", "NÃO FOI POSSÍVEL CONECTAR AO BANCO DE DADOS: "+ e.getMessage());
        }

        return conn;
    }
}


package br.com.bittrexanalizer.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.LinkedList;
import java.util.Set;

import br.com.bittrexanalizer.database.bd.ConnectionFactory;
import br.com.bittrexanalizer.domain.Configuracao;

/**
 * Created by PauLinHo on 24/06/2017.
 */

public class ConfiguracaoDAO implements IDAO<Configuracao> {

    private SQLiteDatabase conn = null;
    private Context context = null;

    public ConfiguracaoDAO(Context context) {
        this.context = context;
    }


    @Override
    public Long create(Configuracao p) {

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("insert into CONFIGURACAO ");
        sql.append("(propriedade, valor) ");
        sql.append("values ( ?, ?)");

        Long iResult = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;

            pstm.bindString(++i, p.getPropriedade().toUpperCase());
            pstm.bindString(++i, p.getValor());


            iResult = pstm.executeInsert();

        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR INSERÇÃO NO BD: " + ex.getMessage());
        }

        conn.close();

        return iResult;
    }


    @Override
    public long update(Configuracao p) {

        long retorno = 0;

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("update CONFIGURACAO ");
        sql.append("set propriedade = ?, valor = ? ");
        sql.append("where id = ?");

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;
            pstm.bindString(++i, p.getPropriedade().toUpperCase());
            pstm.bindString(++i, p.getValor());
            pstm.bindLong(++i, p.getId());

            retorno = pstm.executeUpdateDelete();


        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR UPDATE NO BD: " + ex.getMessage());
            retorno = 0l;
        }

        conn.close();

        return retorno;
    }


    @Override
    public void delete(Configuracao p) {

        try {
            StringBuffer sql = new StringBuffer();
            sql.append("delete from CONFIGURACAO where propriedade = ?");

            //Verifica se a connection é null
            if (conn == null || !conn.isOpen()) {
                conn = ConnectionFactory.getConnection(context);
            }

            SQLiteStatement stm = conn.compileStatement(sql.toString());
            stm.bindString(1, p.getPropriedade());

            stm.executeUpdateDelete();

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();

    }

    @Override
    public Configuracao find(Configuracao j) {

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from CONFIGURACAO where propriedade = ?");

        Configuracao p = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }
        try {
            //Cursor que recebe todos as entidades cadastradas
            String[] arg = {String.valueOf(j.getPropriedade())};
            Cursor cursor = conn.rawQuery(sql.toString(), arg);

            //Se houver primeiro mova para ele
            if (cursor.moveToFirst()) {
                do {
                    p = new Configuracao();
                    //recebendo os dados do banco de dados e armazenando do dominio contato
                    int i = 0;

                    p.setId(cursor.getLong(i));
                    p.setPropriedade(cursor.getString(++i));
                    p.setValor(cursor.getString(++i));


                } while (cursor.moveToNext()); //se existir proximo mova para ele
            }

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }finally {
            conn.close();
        }


        return p;
    }

    @Override
    public Set<Configuracao> findAll() {
        return null;
    }


    public LinkedList<Configuracao> all() {

        LinkedList<Configuracao> lista = new LinkedList<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from CONFIGURACAO order by id asc");

        //Cursor que recebe todas as entidades cadastradas
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                Configuracao p = new Configuracao();
                int i = 0;

                p.setId(cursor.getLong(i));
                p.setPropriedade(cursor.getString(++i));
                p.setValor(cursor.getString(++i));
                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        conn.close();

        return lista;
    }

}


package br.com.bittrexanalizer.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import br.com.bittrexanalizer.api.ApiCredentials;
import br.com.bittrexanalizer.database.bd.ConnectionFactory;

/**
 * Created by PauLinHo on 24/06/2017.
 */

public class ApiCredentialsDAO implements IDAO<ApiCredentials> {

    private SQLiteDatabase conn = null;
    private Context context = null;

    public ApiCredentialsDAO(Context context) {
        this.context = context;
    }


    @Override
    public Long create(ApiCredentials p) {

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("insert into API_CREDENTIALS ");
        sql.append("(key, secret) ");
        sql.append("values ( ?, ?)");

        Long iResult = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;

            pstm.bindString(++i, p.getKey());
            pstm.bindString(++i, p.getSecret());


            iResult = pstm.executeInsert();

        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR INSERÇÃO NO BD: " + ex.getMessage());
        }

        conn.close();

        return iResult;
    }


    @Override
    public long update(ApiCredentials p) {

        long retorno = 0;

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("update API_CREDENTIALS ");
        sql.append("set key = ?, secret = ? ");
        sql.append("where id = ?");

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;
            pstm.bindString(++i, p.getKey());
            pstm.bindString(++i, p.getSecret());
            pstm.bindLong(++i, p.getId());

            retorno = pstm.executeUpdateDelete();


        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR UPDATE NO BD: " + ex.getMessage());
            retorno = 0l;
        }

        conn.close();
        return retorno;
    }


    @Override
    public void delete(ApiCredentials p) {

        try {
            StringBuffer sql = new StringBuffer();
            sql.append("delete from API_CREDENTIALS where id = ?");

            //Verifica se a connection é null
            if (conn == null || !conn.isOpen()) {
                conn = ConnectionFactory.getConnection(context);
            }

            SQLiteStatement stm = conn.compileStatement(sql.toString());
            stm.bindLong(1, p.getId());

            stm.executeUpdateDelete();

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();

    }

    @Override
    public ApiCredentials find(ApiCredentials j) {

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from API_CREDENTIALS where id = ?");

        ApiCredentials p = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }
        try {
            //Cursor que recebe todos as entidades cadastradas
            String[] arg = {String.valueOf(j.getId())};
            Cursor cursor = conn.rawQuery(sql.toString(), arg);

            //Se houver primeiro mova para ele
            if (cursor.moveToFirst()) {
                do {
                    p = new ApiCredentials();
                    //recebendo os dados do banco de dados e armazenando do dominio contato
                    int i = 0;

                    p.setId(cursor.getLong(i));
                    p.setKey(cursor.getString(++i));
                    p.setSecret(cursor.getString(++i));


                } while (cursor.moveToNext()); //se existir proximo mova para ele
            }

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();
        return p;
    }


    @Override
    public synchronized Set<ApiCredentials> findAll() {

        Set<ApiCredentials> lista = new HashSet<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from API_CREDENTIALS order by id asc");

        //Cursor que recebe todas as entidades cadastradas
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                ApiCredentials p = new ApiCredentials();
                int i = 0;

                p.setId(cursor.getLong(i));
                p.setKey(cursor.getString(++i));
                p.setSecret(cursor.getString(++i));
                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        cursor.close();
        conn.close();

        return lista;
    }

    public synchronized LinkedList<ApiCredentials> all() {

        LinkedList<ApiCredentials> lista = new LinkedList<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from API_CREDENTIALS order by id asc");

        //Cursor que recebe todas as entidades cadastradas
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                ApiCredentials p = new ApiCredentials();
                int i = 0;

                p.setId(cursor.getLong(i));
                p.setKey(cursor.getString(++i));
                p.setSecret(cursor.getString(++i));
                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        cursor.close();
        conn.close();

        return lista;
    }

}


package br.com.bittrexanalizer.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import br.com.bittrexanalizer.database.bd.ConnectionFactory;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 24/06/2017.
 */

public class TickerDAO implements IDAO<Ticker> {

    private SQLiteDatabase conn = null;
    private Context context = null;

    public TickerDAO(Context context) {
        this.context = context;
    }


    @Override
    public Long create(Ticker p) {

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("insert into TICKER ");
        sql.append("(nomeExchange, sigla, urlApi, last, bid, ask, isBought, avisoBuyInferior, ");
        sql.append("avisoBuySuperior, avisoStopLoss, avisoStopGain, valorDeCompra ) ");
        sql.append("values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Long iResult = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;

            pstm.bindString(++i, p.getNomeExchange());
            pstm.bindString(++i, p.getSigla());
            pstm.bindString(++i, WebServiceUtil.getUrl() + p.getSigla());
            pstm.bindString(++i, p.getLast().toString());
            pstm.bindString(++i, p.getBid().toString());
            pstm.bindString(++i, p.getAsk().toString());
            pstm.bindString(++i, p.getBought().toString());
            pstm.bindString(++i, p.getAvisoBuyInferior().toString());
            pstm.bindString(++i, p.getAvisoBuySuperior().toString());
            pstm.bindString(++i, p.getAvisoStopLoss().toString());
            pstm.bindString(++i, p.getAvisoStopGain().toString());
            pstm.bindString(++i, p.getValorDeCompra().toString());


            iResult = pstm.executeInsert();

        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR INSERÇÃO NO BD: " + ex.getMessage());
        }

        conn.close();

        return iResult;
    }


    @Override
    public long update(Ticker p) {

        long retorno = 0;

        //Sql de Inserção no BD
        StringBuffer sql = new StringBuffer();
        sql.append("update ticker ");
        sql.append("set nomeExchange = ?, sigla = ?, urlApi = ?, last = ?, bid = ?, ask = ?, isBought = ?, ");
        sql.append("avisoBuyInferior = ?, avisoBuySuperior = ?, avisoStopLoss = ?, avisoStopGain = ?, valorDeCompra = ? ");
        sql.append("where _id = ?");

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        try {
            SQLiteStatement pstm = conn.compileStatement(sql.toString());
            int i = 0;
            pstm.bindString(++i, p.getNomeExchange());
            pstm.bindString(++i, p.getSigla());
            pstm.bindString(++i, WebServiceUtil.getUrl() + p.getSigla());
            pstm.bindString(++i, p.getLast().toString());
            pstm.bindString(++i, p.getBid().toString());
            pstm.bindString(++i, p.getAsk().toString());
            pstm.bindString(++i, p.getBought().toString());
            pstm.bindString(++i, p.getAvisoBuyInferior().toString());
            pstm.bindString(++i, p.getAvisoBuySuperior().toString());
            pstm.bindString(++i, p.getAvisoStopLoss().toString());
            pstm.bindString(++i, p.getAvisoStopGain().toString());
            pstm.bindString(++i, p.getValorDeCompra().toString());
            pstm.bindLong(++i, p.getId());

            retorno = pstm.executeUpdateDelete();


        } catch (Exception ex) {
            Log.i("LOG", "ERRO AO REALIZAR UPDATE NO BD: " + ex.getMessage());
            retorno = 0l;
        }

        conn.close();

        return retorno;
    }


    @Override
    public void delete(Ticker p) {

        try {
            StringBuffer sql = new StringBuffer();
            sql.append("delete from TICKER where sigla = ?");

            //Verifica se a connection é null
            if (conn == null || !conn.isOpen()) {
                conn = ConnectionFactory.getConnection(context);
            }

            SQLiteStatement stm = conn.compileStatement(sql.toString());
            stm.bindString(1, p.getSigla());

            stm.executeUpdateDelete();

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();

    }

    @Override
    public Ticker find(Ticker j) {

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from TICKER where nomeExchange = ?");

        Ticker p = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }
        try {
            //Cursor que recebe todos as entidades cadastradas
            String[] arg = {String.valueOf(j.getNomeExchange())};
            Cursor cursor = conn.rawQuery(sql.toString(), arg);

            //Se houver primeiro mova para ele
            if (cursor.moveToFirst()) {
                do {
                    p = getTicker(cursor);

                } while (cursor.moveToNext()); //se existir proximo mova para ele
            }

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();
        return p;
    }

    public LinkedList<Ticker> findAllIsBought(Boolean valor) {

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from TICKER where isBought = ?");

        LinkedList<Ticker> tickers = new LinkedList<>();

        Ticker p = null;

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }
        try {
            //Cursor que recebe todos as entidades cadastradas
            String[] arg = {String.valueOf(valor)};
            Cursor cursor = conn.rawQuery(sql.toString(), arg);

            //Se houver primeiro mova para ele
            if (cursor.moveToFirst()) {
                do {
                    p = getTicker(cursor);

                    tickers.add(p);

                } while (cursor.moveToNext()); //se existir proximo mova para ele
            }

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        }

        conn.close();
        return tickers;
    }


    @Override
    public synchronized Set<Ticker> findAll() {

        Set<Ticker> lista = new HashSet<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from Ticker order by sigla asc");

        //Cursor que recebe todas as entidades cadastradas
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                Ticker p = getTicker(cursor);
                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        conn.close();

        return lista;
    }

    public LinkedList<Ticker> findAllTickers() {

        LinkedList<Ticker> lista = new LinkedList<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from Ticker order by sigla asc");

        //Cursor que recebe todas as entidades cadastradas
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                Ticker p = getTicker(cursor);
                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        conn.close();

        return lista;
    }

    public synchronized Set<Ticker> findAllByBought(Ticker j) {

        Set<Ticker> lista = new HashSet<>();

        //abrindo a conexao com o Banco de DAdos
        if (conn == null || !conn.isOpen()) {
            conn = ConnectionFactory.getConnection(context);
        }

        //sql de select para o BD
        StringBuffer sql = new StringBuffer();
        sql.append("select * from Ticker where isBought = ?");

        //Cursor que recebe todas as entidades cadastradas
        String[] arg = {String.valueOf(j.getBought())};
        Cursor cursor = conn.rawQuery(sql.toString(), null);

        //Existe Dados?
        if (cursor.moveToFirst()) {
            do {

                //recebendo os dados do banco de dados e armazenando do dominio contato
                Ticker p = getTicker(cursor);

                //add a entidade na lista
                lista.add(p);


            } while (cursor.moveToNext()); //se existir proximo mova para ele
        }

        conn.close();
        return lista;
    }

    private Ticker getTicker(Cursor cursor){

        Ticker p = new Ticker();
        int i = 0;

        p.setId(cursor.getLong(i));
        p.setNomeExchange(cursor.getString(++i));
        p.setSigla(cursor.getString(++i));
        p.setUrlApi(cursor.getString(++i));
        p.setLast(new BigDecimal(cursor.getString(++i)));
        p.setBid(new BigDecimal(cursor.getString(++i)));
        p.setAsk(new BigDecimal(cursor.getString(++i)));
        p.setBought(Boolean.valueOf(cursor.getString(++i)));
        p.setAvisoBuyInferior(new BigDecimal(cursor.getString(++i)));
        p.setAvisoBuySuperior(new BigDecimal(cursor.getString(++i)));
        p.setAvisoStopLoss(new BigDecimal(cursor.getString(++i)));
        p.setAvisoStopGain(new BigDecimal(cursor.getString(++i)));
        p.setValorDeCompra(new BigDecimal(cursor.getString(++i)));

        return p;
    }

}


package br.com.bittrexanalizer.database.dao;

import java.util.Set;

/**
 * Created by PauLinHo on 24/06/2017.
 */

/**
 * Interface IDAO implementada por todas classes que persistem objetos no BD
 */
public interface IDAO<T extends Object> {

    /**
     *
     * @param - Recebe uma entidade Dominio para persistir no BD
     * @return Um Long com o id da entidade armazenada no BD
     */
    Long create(T p);

    /**
     * @return retorno o numero de linhas afetadas ou 0 se houve erro
     * @param p recebe uma entidade Dominio e altera a entidade com o mesmo id no BD
     */
    long update(T p);

    /**
     *
     * @param p recebe uma entidade Dominio e remove a entidade com o mesmo id no BD
     */
    void delete(T p);

    /**
     *
     * @param p recebe uma entidade dominio e pesquisa no BD a entidade com o mesmo id
     * @return uma entidade do bd ou null se nao encontrar
     */
    T find(T p);

    /**
     *
     * @return todas as entidades no BD
     */
    Set<T> findAll();
}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.BigDecimalComparator;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 12/01/2018.
 */

public class OsciladorEstocasticoAnaliser implements IAnaliser<Candle> {

    private Integer periodoK = new Integer(14);
    private Integer periodoD = new Integer(3);
    private int retorno = SEM_OSCILACAO;
    private static final int SET_SCALE =8;

    private static final String CRITERIO_ORDENACAO_LOW_ASC = "LOW ASC";
    private static final String CRITERIO_ORDENACAO_HIGH_ASC = "HIGH ASC";
    private static final String CRITERIO_ORDENACAO_CLOSE_ASC = "CLOSE ASC";

    public OsciladorEstocasticoAnaliser() {
         periodoK = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OE_TEMPO_PERIODO_K));
        periodoD = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OE_TEMPO_PERIODO_D));
    }

    @Override
    public int analizer(LinkedList<Candle> candles) {


        LinkedList<BigDecimal> fechamentos = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> minimas = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> maximas = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaPeriodoK = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaPeriodoD = new LinkedList<BigDecimal>();

        //verifica o tamanho dos dados se for menor que o PERIODO k não sera possivel realizar a Analise
        if (getCandles(candles, periodoK) == null) {
            return -ERRO;
        }

        fechamentos = getPeriodosFechamentos(getCandles(candles, periodoK));
        maximas = getMaximas(getCandles(candles, periodoK));
        minimas = getMinimas(getCandles(candles, periodoK));

        listaPeriodoK = getPeriodoK(fechamentos, maximas, minimas);
        listaPeriodoD = getPeriodoD(listaPeriodoK);


        retorno = calcular(listaPeriodoK, listaPeriodoD);

        return retorno;

    }

    private int calcular(LinkedList<BigDecimal> listaPeriodoK, LinkedList<BigDecimal> listaPeriodoD) {

        int retorno = SEM_OSCILACAO;

        int tamanhoTotal = listaPeriodoK.size();

        if (listaPeriodoK.get(tamanhoTotal - 1).compareTo(
                new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OE_TAXA_MIN))) == -1 &&
                listaPeriodoK.get(tamanhoTotal).compareTo(
                        new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OE_TAXA_MIN))) == 1) {
            return IDEAL_PARA_COMPRA;

        }

        return retorno;

    }

    private LinkedList<BigDecimal> getPeriodosFechamentos(LinkedList<Candle> dados) {

        LinkedList<BigDecimal> listaResultado = new LinkedList<BigDecimal>();

        for (int j = 0; j < dados.size(); j++) {
            listaResultado.add(dados.get(j).getC().setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN));
        }

        return listaResultado;

    }

    private LinkedList<BigDecimal> getMaximas(LinkedList<Candle> dados) {

        LinkedList<BigDecimal> listaResultado = new LinkedList<BigDecimal>();

        for (int j = 0; j < dados.size(); j++) {
            listaResultado.add(dados.get(j).getH().setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN));
        }

        return listaResultado;

    }

    private LinkedList<BigDecimal> getMinimas(LinkedList<Candle> dados) {

        LinkedList<BigDecimal> listaResultado = new LinkedList<BigDecimal>();

        for (int j = 0; j < dados.size(); j++) {
            listaResultado.add(dados.get(j).getL().setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN));
        }

        return listaResultado;

    }


    private LinkedList<BigDecimal> getPeriodoK(LinkedList<BigDecimal> fechamentos, LinkedList<BigDecimal> maximas, LinkedList<BigDecimal> minimas) {

        //se(fechamento.get(11)<>0;(fechamento.get(11)-minimo(d7:d11))/maximo(e7:e11)-minimo(d7:d11));0)

        LinkedList<BigDecimal> listaPeriodoK = new LinkedList<>();


        for (int i = 0; i < fechamentos.size(); i++) {

            if (i + periodoK > fechamentos.size()) {
                i = fechamentos.size();
                continue;
            }

            BigDecimal fechamentoTemp = new BigDecimal("0").setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal aux = new BigDecimal("0").setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal minimaTemp = new BigDecimal("0").setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal maximaTemp = new BigDecimal("0").setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);
            LinkedList<BigDecimal> maximasTemp = copyOfRange(maximas, i, periodoK);
            LinkedList<BigDecimal> minimasTemp = copyOfRange(minimas, i, periodoK);

            fechamentoTemp = fechamentos.get(i + (periodoK - 1)).setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);

            maximasTemp = BigDecimalComparator.ordenar(CRITERIO_ORDENACAO_HIGH_ASC, maximasTemp);
            minimasTemp = BigDecimalComparator.ordenar(CRITERIO_ORDENACAO_LOW_ASC, minimasTemp);

            minimaTemp = minimasTemp.getFirst().setScale(SET_SCALE, RoundingMode.HALF_EVEN);
            fechamentoTemp = fechamentoTemp.subtract(minimaTemp).setScale(SET_SCALE, RoundingMode.HALF_EVEN);

            maximaTemp = maximasTemp.getLast().setScale(SET_SCALE, RoundingMode.HALF_EVEN);

            aux = maximaTemp.subtract(minimaTemp).setScale(SET_SCALE, RoundingMode.HALF_EVEN);

            BigDecimal retorno = fechamentoTemp.divide(aux, BigDecimal.ROUND_HALF_EVEN).setScale(SET_SCALE, RoundingMode.HALF_EVEN);

            listaPeriodoK.add(retorno.multiply(new BigDecimal("100")));

        }

        return listaPeriodoK;

    }

    private LinkedList<BigDecimal> getPeriodoD(LinkedList<BigDecimal> listaPeriodoK) {

        //=se(fechamento<>0;media(k11:k13)

        LinkedList<BigDecimal> listaPeriodoD = new LinkedList<>();


        for (int i = 0; i < listaPeriodoK.size(); i++) {

            if (i + periodoD > listaPeriodoK.size()) {
                i = listaPeriodoK.size();
                continue;
            }

            BigDecimal aux = new BigDecimal("0").setScale(SET_SCALE, BigDecimal.ROUND_HALF_EVEN);
            LinkedList<BigDecimal> intervaloDeMedia = copyOfRange(listaPeriodoK, i, periodoD);

            for (BigDecimal b : intervaloDeMedia) {
                aux = aux.add(b);
            }

            aux = aux.divide(new BigDecimal(periodoD), BigDecimal.ROUND_HALF_EVEN).setScale(SET_SCALE, RoundingMode.HALF_EVEN);


            listaPeriodoD.add(aux);

        }

        return listaPeriodoD;

    }

    private LinkedList<BigDecimal> copyOfRange(LinkedList<BigDecimal> original, int inicio, int tamanho) {

        LinkedList<BigDecimal> retorno = new LinkedList<>();


        for (int i = inicio; i < (inicio + tamanho); i++) {
            retorno.add(original.get(i));
        }

        return retorno;

    }


    private static LinkedList<Candle> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<Candle> candlesRetorno = new LinkedList<>();

        if (candles.size() < (qtde * 2)) {
            return null;
        }

        for (int i = candles.size() - ((qtde * 2) + 1); i < candles.size()-1; i++) {
            candlesRetorno.add(candles.get(i));
        }

        return candlesRetorno;

    }


}


package br.com.bittrexanalizer.analises;

import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.domain.EntidadeDomain;

/**
 * Created by PauLinHo on 12/01/2018.
 */

public interface IAnaliser<T extends EntidadeDomain> {

    int ERRO = -2;
    int IDEAL_PARA_VENDA = -1;
    int SEM_OSCILACAO = 0;
    int IDEAL_PARA_COMPRA = 1;
    String COMPRA = "COMPRA";
    String VENDA = "VENDA";

    /**
     * Executa a verificação na Analize escolhida
     * @return -2 ERRO
     *         -1 IDEAL PARA VENDA
     *          0 SEM OSCILAÇÂO
     *          1 IDEAL PARA COMPRA
     */
    int analizer(LinkedList<Candle> candles);


}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

public class OBVAnaliser implements IAnaliser<Candle> {

    private LinkedList<BigDecimal> listaOBV;
    private int qtdeDiasOBV = 0;
    private int qtdeFechamentosOBV = 0;
    private int retorno = SEM_OSCILACAO;


    public OBVAnaliser() {

        listaOBV = new LinkedList<>();

        this.qtdeFechamentosOBV = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OBV_QTDE_FECHAMENTOS));
        qtdeDiasOBV = qtdeFechamentosOBV *2;
    }

    @Override
    public int analizer(LinkedList<Candle> candlesTotal) {

        // IFR = 100 -(100 / (1 +FR)) ***FR= (MediaDeGanhos / MediaDePerdas)


        //verifica o tamanho dos dados se for menor que o QTDERDIASIFR não sera possivel realizar a Analizse
        if (getCandles(candlesTotal, qtdeDiasOBV) == null) {
            return -ERRO;
        }

        //Filtra a lista com apenas a quantidadeDesejada
        LinkedList<Candle> candles = getCandles(candlesTotal, qtdeDiasOBV);

        BigDecimal aux = BigDecimal.ZERO;

        //=SE(E3 = E2; H2; SE(E3 > E2; H2 + G3; H2 - G3 ))
        for (int i = 0; i < candles.size(); i++) {
            if (i == 0) {
                listaOBV.add(candles.get(i).getV());
            } else {
                //É igual?
                if (candles.get(i).getC().compareTo(candles.get(i - 1).getC()) == 0) {
                    listaOBV.add(listaOBV.get(i - 1).add(BigDecimal.ZERO));
                } else if (candles.get(i).getC().compareTo(candles.get(i - 1).getC()) == 1) {
                    listaOBV.add(listaOBV.get(i - 1).add(candles.get(i).getV()));
                } else {
                    listaOBV.add(listaOBV.get(i - 1).subtract(candles.get(i).getV()));
                }
            }
        }


        retorno = calcular(listaOBV, candles);

        return retorno;

    }

    private int calcular(LinkedList<BigDecimal> listaOBV, LinkedList<Candle> candles) {


        int retorno = SEM_OSCILACAO;
        boolean devoComprar = false;

        int indice = listaOBV.size() - qtdeFechamentosOBV;

        for (; indice < listaOBV.size(); indice++) {
            if (listaOBV.get(indice).compareTo(listaOBV.get(indice-1)) == 1 &&
                    candles.get(indice).getC().compareTo(candles.get(indice-1).getC())== 1) {
                devoComprar = true;
            } else {
                devoComprar = false;
                return retorno;
            }
        }

        if(devoComprar){
            retorno = IDEAL_PARA_COMPRA;
        }

        return retorno;
    }

    private static LinkedList<Candle> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<Candle> candlesRetorno = new LinkedList<>();

        if (candles.size() < qtde) {
            return null;
        }

        for (int i = candles.size() - qtde; i < candles.size()-1; i++) {
            candlesRetorno.add(candles.get(i));
        }

        return candlesRetorno;

    }


}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

public class VolumeAnaliser implements IAnaliser<Candle> {

    private LinkedList<BigDecimal> listaOBV;
    private int qtdeDiasOBV = 0;
    private int qtdeFechamentosOBV = 0;
    private int retorno = SEM_OSCILACAO;


    public VolumeAnaliser() {

        listaOBV = new LinkedList<>();

        this.qtdeFechamentosOBV = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OBV_QTDE_FECHAMENTOS));
        qtdeDiasOBV = qtdeFechamentosOBV *2;
    }

    @Override
    public int analizer(LinkedList<Candle> candlesTotal) {

        // IFR = 100 -(100 / (1 +FR)) ***FR= (MediaDeGanhos / MediaDePerdas)


        //verifica o tamanho dos dados se for menor que o QTDERDIASIFR não sera possivel realizar a Analizse
        if (getCandles(candlesTotal, qtdeDiasOBV) == null) {
            return -ERRO;
        }

        //Filtra a lista com apenas a quantidadeDesejada
        LinkedList<Candle> candles = getCandles(candlesTotal, qtdeDiasOBV);

        BigDecimal aux = BigDecimal.ZERO;

        //=SE(E3 = E2; H2; SE(E3 > E2; H2 + G3; H2 - G3 ))
        for (int i = 0; i < candles.size(); i++) {
            if (i == 0) {
                listaOBV.add(candles.get(i).getV());
            } else {
                //É igual?
                if (candles.get(i).getC().compareTo(candles.get(i - 1).getC()) == 0) {
                    listaOBV.add(listaOBV.get(i - 1).add(BigDecimal.ZERO));
                } else if (candles.get(i).getC().compareTo(candles.get(i - 1).getC()) == 1) {
                    listaOBV.add(listaOBV.get(i - 1).add(candles.get(i).getV()));
                } else {
                    listaOBV.add(listaOBV.get(i - 1).subtract(candles.get(i).getV()));
                }
            }
        }


        retorno = calcular(listaOBV, candles);

        return retorno;

    }

    private int calcular(LinkedList<BigDecimal> listaOBV, LinkedList<Candle> candles) {


        int retorno = SEM_OSCILACAO;
        boolean devoComprar = false;

        int indice = listaOBV.size() - qtdeFechamentosOBV;

        for (; indice < listaOBV.size(); indice++) {
            if (listaOBV.get(indice).compareTo(listaOBV.get(indice-1)) == 1) {
                devoComprar = true;
            } else {
                devoComprar = false;
                return retorno;
            }
        }

        if(devoComprar){
            retorno = IDEAL_PARA_COMPRA;
        }

        return retorno;
    }

    private static LinkedList<Candle> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<Candle> candlesRetorno = new LinkedList<>();

        if (candles.size() < qtde) {
            return null;
        }

        for (int i = candles.size() - qtde; i < candles.size(); i++) {
            candlesRetorno.add(candles.get(i));
        }

        return candlesRetorno;

    }


}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 12/01/2018.
 */

public class MACDAnaliser implements IAnaliser<Candle> {

    private Integer qtdeDiasLongEMA = new Integer(26);
    private Integer qtdeDiasShortEMA = new Integer(12);
    private Integer qtdeDiasSIGNAL = new Integer(9);
    private String sigla;
    private int retorno = SEM_OSCILACAO;

    public MACDAnaliser() {
        qtdeDiasLongEMA = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.LONG_EMA));
        qtdeDiasShortEMA = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.SHORT_EMA));
        qtdeDiasSIGNAL = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.SIGNAL));
    }

    @Override
    public int analizer(LinkedList<Candle> candles) {

        sigla = candles.getFirst().getSigla();

        LinkedList<BigDecimal> listaShortEMA = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaLongEMA = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaMACD = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaSignal = new LinkedList<BigDecimal>();
        LinkedList<BigDecimal> listaHistogram = new LinkedList<BigDecimal>();

        //verifica o tamanho dos dados se for menor que o EMA LONG não sera possivel realizar a Analise
        if(getCandles(candles, qtdeDiasLongEMA)==null){
            return -ERRO;
        }

        listaShortEMA = getListaEMA(getCandles(candles, qtdeDiasShortEMA), qtdeDiasShortEMA);
        listaLongEMA = getListaEMA(getCandles(candles, qtdeDiasLongEMA), qtdeDiasLongEMA);
        listaMACD = getMACD(listaShortEMA, listaLongEMA);
        listaSignal = getListaSignal(listaMACD, qtdeDiasSIGNAL);
        listaHistogram = getHistogram(listaMACD, listaSignal);

        retorno = calcular(listaMACD, listaSignal, listaHistogram);

        return retorno;

    }

    private int calcular(LinkedList<BigDecimal> listaMACD, LinkedList<BigDecimal> listaSignal, LinkedList<BigDecimal> listaHistogram) {

        int retorno = SEM_OSCILACAO;

        if (listaMACD.getLast().compareTo(BigDecimal.ZERO.setScale(8, BigDecimal.ROUND_HALF_EVEN)) == -1) {
            return IDEAL_PARA_VENDA;
        }

        int tamanhoListaHistograma = listaHistogram.size();

//        for (int i = listaHistogram.size() - 2; i < listaHistogram.size(); i++) {
//            if (listaHistogram.get(tamanhoListaHistograma - 2).compareTo(BigDecimal.ZERO) == -1) {
//                if (listaHistogram.get(tamanhoListaHistograma - 1).compareTo(BigDecimal.ZERO) == 1) {
//                    return IDEAL_PARA_COMPRA;
//                }
//            }
//        }

        BigDecimal ultimoMACD = listaMACD.getLast();
        BigDecimal ultimoSignal = listaSignal.getLast();

        if(ultimoMACD.compareTo(BigDecimal.ZERO.setScale(8))==1){
            if(ultimoMACD.compareTo(ultimoSignal)==1){
                retorno = IDEAL_PARA_COMPRA;
            }
        }

        return retorno;

    }

    private LinkedList<BigDecimal> getListaEMA(LinkedList<Candle> dados, int qtdeDias) {

        LinkedList<BigDecimal> listaResultado = new LinkedList<BigDecimal>();

        BigDecimal mediaAnterior = new BigDecimal("0");
        mediaAnterior = getMedia(dados, qtdeDias);

        for (int j = qtdeDias; j < dados.size(); j++) {
            listaResultado.add(calcularEMA(dados.get(j), qtdeDias, mediaAnterior, j));
            mediaAnterior = listaResultado.getLast();
        }

        return listaResultado;

    }

    private LinkedList<BigDecimal> getListaSignal(LinkedList<BigDecimal> listaMACD, int qtdeDias) {

        LinkedList<BigDecimal> listaResultado = new LinkedList<BigDecimal>();

        BigDecimal mediaAnterior = new BigDecimal("0");
        BigDecimal porcentagem = new BigDecimal("0");
        BigDecimal resultado = new BigDecimal("0");

        for (int i = 0; i < qtdeDias; i++) {
            mediaAnterior = mediaAnterior.add(listaMACD.get(i));
        }

        mediaAnterior = mediaAnterior.divide(new BigDecimal(qtdeDias), 8, RoundingMode.HALF_EVEN);

        porcentagem = getPorcentagem(qtdeDias);

        for (int i = listaMACD.size() - qtdeDias; i < listaMACD.size(); i++) {
            resultado = new BigDecimal("0");
            resultado = listaMACD.get(i).subtract(mediaAnterior);
            resultado = resultado.multiply(porcentagem);
            resultado = resultado.add(mediaAnterior).setScale(8, RoundingMode.HALF_EVEN);

            listaResultado.add(resultado);
        }

        return listaResultado;

    }

    private BigDecimal getMedia(LinkedList<Candle> lista, int qtdeDias) {
        BigDecimal media = new BigDecimal("0");

        for (int i = 0; i < qtdeDias; i++) {
            media = media.add(lista.get(i).getC());
        }

        media = media.divide(new BigDecimal(qtdeDias), 8, RoundingMode.HALF_EVEN);

        return media;

    }

    private BigDecimal calcularEMA(Candle candle, int qtdeDias, BigDecimal mediaAnterior, int contador) {

        BigDecimal resultado = new BigDecimal("0.0");
        BigDecimal porcentagem = getPorcentagem(qtdeDias);

        resultado = new BigDecimal("0");
        resultado = candle.getC().subtract(mediaAnterior);
        resultado = resultado.multiply(porcentagem);
        resultado = resultado.add(mediaAnterior).setScale(8, RoundingMode.HALF_EVEN);

        return resultado;

    }

    /**
     * Calcula a porcentagem para realizar o calculo das EMA
     *
     * @param qtdeDias - A quantidade em Dias que terá a EMA
     * @return - Um BIGDecimal com a porcentagem
     */
    private BigDecimal getPorcentagem(Integer qtdeDias) {

        BigDecimal porcentagem = new BigDecimal("0");

        qtdeDias += 1;

        porcentagem = new BigDecimal(2).divide(new BigDecimal(qtdeDias), 8, RoundingMode.HALF_EVEN);

        return porcentagem;

    }

    private LinkedList<BigDecimal> getMACD(LinkedList<BigDecimal> listaShortEMA, LinkedList<BigDecimal> listaLongEMA) {

        LinkedList<BigDecimal> listaMACD = new LinkedList<>();
        BigDecimal temp = null;

        for (int i = 0; i < listaShortEMA.size(); i++) {

            temp = new BigDecimal("0");

            temp = listaShortEMA.get(i).subtract(listaLongEMA.get(i));

            listaMACD.add(temp);

        }

        return listaMACD;

    }

    private LinkedList<BigDecimal> getHistogram(LinkedList<BigDecimal> listaMACD, LinkedList<BigDecimal> listaSignal) {

        LinkedList<BigDecimal> listaHistogram = new LinkedList<>();
        BigDecimal temp = null;

        for (int i = 0; i < listaSignal.size(); i++) {

            temp = new BigDecimal("0");

            temp = listaMACD.get(i).subtract(listaSignal.get(i));

            listaHistogram.add(temp);

        }

        return listaHistogram;

    }

    private static LinkedList<Candle> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<Candle> candlesRetorno = new LinkedList<>();

        if (candles.size() < (qtde*3)) {
            return null;
        }

        for (int i = candles.size() - (qtde*3); i < candles.size()-1; i++) {
            candlesRetorno.add(candles.get(i));
        }

        return candlesRetorno;

    }


}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

public class IFRAnaliser implements IAnaliser<Candle> {

    private BigDecimal IFR;
    private BigDecimal mediaDeGanhos;
    private BigDecimal mediaDePerdas;
    private int qtdeDiasIFR = 0;
    private int retorno = SEM_OSCILACAO;


    public IFRAnaliser(){

        this.qtdeDiasIFR = new Integer(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.IFR_DIAS));
    }

    @Override
    public int analizer(LinkedList<Candle> candlesTotal) {

        // IFR = 100 -(100 / (1 +FR)) ***FR= (MediaDeGanhos / MediaDePerdas)


        //verifica o tamanho dos dados se for menor que o QTDERDIASIFR não sera possivel realizar a Analizse
        if(getCandles(candlesTotal, qtdeDiasIFR)==null){
            return -ERRO;
        }

        //Filtra a lista com apenas a quantidadeDesejada
        LinkedList<Candle> candles = getCandles(candlesTotal, qtdeDiasIFR);

        int qtdeDiasNegativo = 0;
        int qtdeDiasPositivo = 0;
        BigDecimal valorNegativoSomado = new BigDecimal("0.0");
        BigDecimal valorPositivoSomado = new BigDecimal("0.0");

        // pega os valores
        for (int i = 0; i < candles.size() - 1; i++) {

            // negativo
            if (candles.get(i).getC().compareTo(candles.get(i + 1).getC()) == 1) {
                qtdeDiasNegativo++;
                valorNegativoSomado = valorNegativoSomado
                        .add(candles.get(i).getC().subtract(candles.get(i + 1).getC()));
            } else if (candles.get(i).getC().compareTo(candles.get(i + 1).getC()) == -1) {
                qtdeDiasPositivo++;
                valorPositivoSomado = valorPositivoSomado
                        .add(candles.get(i + 1).getC().subtract(candles.get(i).getC()));
            }

        }

        // efetua o calculo
        mediaDePerdas = BigDecimal.ZERO;
        mediaDePerdas = valorNegativoSomado.divide(new BigDecimal(qtdeDiasNegativo), 8, RoundingMode.HALF_EVEN);

        mediaDeGanhos = BigDecimal.ZERO;
        mediaDeGanhos = valorPositivoSomado.divide(new BigDecimal(qtdeDiasPositivo), 8, RoundingMode.HALF_EVEN);

        // IFR = 100 -(100 / (1 +FR)) ***FR= (MediaDeGanhos / MediaDePerdas)

        IFR = BigDecimal.ZERO;

        BigDecimal fr = BigDecimal.ZERO;

        fr = mediaDeGanhos.divide(mediaDePerdas, 8, RoundingMode.HALF_EVEN);

        BigDecimal aux = BigDecimal.ONE.add(fr);
        BigDecimal aux2 = new BigDecimal("100").divide(aux, 8, RoundingMode.HALF_EVEN);

        IFR = new BigDecimal("100").subtract(aux2);
        IFR = IFR.subtract(new BigDecimal(5.15));

        retorno = calcular(IFR);

        return retorno;

    }

    private int calcular(BigDecimal IFR) {


        int retorno = SEM_OSCILACAO;

        if(IFR.compareTo(new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get("IFR_MIN")))==-1){
            return IDEAL_PARA_COMPRA;
        }

        if(IFR.compareTo(new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get("IFR_MAX")))==1){
            return IDEAL_PARA_VENDA;
        }

        return retorno;
    }

    private static LinkedList<Candle> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<Candle> candlesRetorno = new LinkedList<>();

        if(candles.size()<qtde*3){
            return null;
        }

        for (int i = candles.size()-qtde*3; i < candles.size()-1; i++) {
            candlesRetorno.add(candles.get(i));
        }

        return candlesRetorno;

    }


}


package br.com.bittrexanalizer.analises;

import java.math.BigDecimal;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 06/02/2018.
 */

public class AnaliseRobot implements IAnaliser<Candle> {

    private LinkedList<BigDecimal> lasts;
    private LinkedList<BigDecimal> volumes;
    private BigDecimal mediaVolume;
    private int numeroDeVerificacoes;

    public AnaliseRobot(){
        lasts = new LinkedList<>();
        volumes = new LinkedList<>();
        mediaVolume = BigDecimal.ZERO.setScale(8);
        numeroDeVerificacoes = Integer.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ROBOT_VEZES));
    }


    @Override
    public int analizer(LinkedList<Candle> candles) {


        //verifica o tamanho dos dados se for menor que o QTDERDIASIFR não sera possivel realizar a Analizse
        if(getCandles(candles, numeroDeVerificacoes)==null){
            return -ERRO;
        }

        lasts = getCandles(candles, numeroDeVerificacoes);
        volumes = getVolumes(candles, numeroDeVerificacoes*2);

        for(BigDecimal b : volumes){
            mediaVolume.add(b);
        }

        mediaVolume = mediaVolume.divide(new BigDecimal(volumes.size()), BigDecimal.ROUND_HALF_EVEN);

        //Se o volume não for maior que a média de volume
        if(mediaVolume.compareTo(volumes.get(volumes.size()-2))==-1){
            return IFRAnaliser.SEM_OSCILACAO;
        }

        for(int i = 0; i < lasts.size()-1; i++){
            if(!devoComprar(lasts.get(i), lasts.get(i+1))){
                return IFRAnaliser.SEM_OSCILACAO;
            }
        }




        return IFRAnaliser.IDEAL_PARA_COMPRA;
    }

    private boolean devoComprar(BigDecimal ultimo, BigDecimal atual){

        //o atual é maior
        if(atual.compareTo(ultimo)!=-1){
            return true;
        }else{
            return false;
        }

    }

    private static LinkedList<BigDecimal> getCandles(LinkedList<Candle> candles, int qtde) {

        LinkedList<BigDecimal> candlesRetorno = new LinkedList<>();

        if (candles.size() < qtde) {
            return null;
        }

        for (int i = candles.size() - qtde; i < candles.size(); i++) {
            candlesRetorno.add(candles.get(i).getL());
        }

        return candlesRetorno;

    }

    private static LinkedList<BigDecimal> getVolumes(LinkedList<Candle> candles, int qtde) {

        LinkedList<BigDecimal> candlesRetorno = new LinkedList<>();

        if (candles.size() < qtde) {
            return null;
        }

        for (int i = candles.size() - qtde; i < candles.size(); i++) {
            candlesRetorno.add(candles.get(i).getV());
        }

        return candlesRetorno;

    }
}


package br.com.bittrexanalizer.facade;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.bittrexanalizer.database.dao.ConfiguracaoDAO;
import br.com.bittrexanalizer.database.dao.TickerDAO;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.domain.Configuracao;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.strategy.BalanceStrategy;
import br.com.bittrexanalizer.strategy.SellOrderStrategy;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 16/09/2017.
 */

public class VendaFacade {

    private TickerDAO tickerDAO;
    private boolean robotLigado = false;
    private EmailUtil emailUtil;
    private Context context;

    private final String ROBOT_VENDA = "VENDA ROBOT ";
    private final String ROBOT_ERROS = "ERROS ROBOT ";

    private StringBuilder moedasErros = new StringBuilder();

    private volatile LinkedList<Ticker> moedasHabilitadas;

    public void executar(Context context) {

        this.context = context;
        tickerDAO = new TickerDAO(context);

        Log.i("MENU", "VENDA FACADE");

        getConfiguracoes();

        moedasHabilitadas = new LinkedList<>();
        moedasHabilitadas = tickerDAO.findAllIsBought(true);

        robotLigado = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ROBOT_LIGADO));

        moedasErros.append("\r\r\r"+ROBOT_ERROS);

        emailUtil = new EmailUtil();

        if (moedasHabilitadas.size() == 0) {
            return;
        } else {

            //Verifica se o robot esta ligado
            if (robotLigado) {
                executarRobot();
                SessionUtil.getInstance().getMsgErros().append(moedasErros.toString());
            }else{
                return;
            }
        }


    }

    private void getConfiguracoes() {

        LinkedList<Configuracao> configuracoes = new LinkedList<>();
        configuracoes = new ConfiguracaoDAO(context).all();

        Map<String, String> mapConfiguracao = new HashMap<String, String>();

        if (configuracoes == null) {
            SessionUtil.getInstance().setMapConfiguracao(null);
            return;
        }

        for (Configuracao c : configuracoes) {
            mapConfiguracao.put(c.getPropriedade(), c.getValor());
        }

        SessionUtil.getInstance().setMapConfiguracao(mapConfiguracao);


    }


    private void executarRobot() {


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    getDados();

                    boolean vendido = false;

                    for(Ticker t : moedasHabilitadas) {

                        if (t.getAvisoStopGain().compareTo(t.getBid()) == -1) {
                            vendido = venderLimit(t);
                        }

                        if (t.getAvisoStopLoss().compareTo(t.getAsk()) == 1) {
                            vendido = venderLoss(t);
                        }

                        if (vendido) {
                            t.setBought(false);
                            tickerDAO.update(t);
                        }
                    }

                } catch (Exception e) {
                    moedasErros.append("ERRO: " + e.getMessage());
                }


            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    private synchronized boolean venderLimit(Ticker ticker) {

        Order order = new Order();
        //verifica se tem uma order aberta para essa moeda

        //Atualiza o map de Balances
        BalanceStrategy.execute();

        //Se não existir a ordem pega a quantidade total dessa moeda
        Balance b = SessionUtil.getInstance().getMapBalances().get(ticker.getSigla());
        order.setQuantity(b.getBalance());
        order.setSigla(ticker.getSigla());

        //Pega o valor atual de venda
        order.setRate(ticker.getBid());

        //Realiza a venda
        boolean vendida = SellOrderStrategy.execute(order);

        if (vendida) {
            String mensagem = ROBOT_VENDA + "Moeda: " + ticker.getSigla();
            mensagem += "\n - Valor: " + ticker.getAsk();
            mensagem += "\n - Valor Limit: " + ticker.getAvisoStopGain();
            mensagem += "\n - Valor Lost: " + ticker.getAvisoStopLoss();
            enviarEmail(mensagem, ROBOT_VENDA);
        }else{
            moedasErros.append("Erro ao vender a moeda: "+ticker.getSigla());
        }

        return vendida;

    }

    private synchronized boolean venderLoss(Ticker ticker) {

        Order order = new Order();
        //verifica se tem uma order aberta para essa moeda

        //Atualiza o map de Balances
        BalanceStrategy.execute();

        //Se não existir a ordem pega a quantidade total dessa moeda
        Balance b = SessionUtil.getInstance().getMapBalances().get(ticker.getSigla());
        order.setQuantity(b.getBalance());
        order.setSigla(ticker.getSigla());

        //Pega o valor atual de venda
        order.setRate(ticker.getBid());

        //Realiza a venda
        boolean vendeu = false;
        vendeu = SellOrderStrategy.execute(order);

        if (!vendeu) {
            moedasErros.append("Não foi possível realizar a venda");
        }

        return vendeu;

    }

    private void enviarEmail(final String mensagem, final String operacao) {
        emailUtil.enviarEmail(context, ROBOT_VENDA + mensagem, operacao);

    }

    /**
     * Pega os dados do bittrex
     */
    public synchronized void getDados() {

        try {

            ExecutorService executorService = Executors.newCachedThreadPool();

            for (Ticker ticker : moedasHabilitadas) {
                executorService.execute(ticker);
            }

            executorService.shutdown();

            while (!executorService.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            return;

        }
    }


}


package br.com.bittrexanalizer.facade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.database.dao.TickerDAO;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.telas.MainActivityDrawer;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.WebServiceUtil;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by PauLinHo on 16/09/2017.
 */

public class AlarmTaxaFacade implements Runnable {

    private Ticker ticker;

    private TickerDAO tickerDAO;
    private Context context;
    private StringBuilder descricao;

    private EmailUtil emailUtil;

    private final String LOG = "BITTREX";
    private volatile LinkedList<Ticker> tickers;


    public synchronized void execute(Context context) {

        this.context = context;
        tickerDAO = new TickerDAO(context);
        emailUtil = new EmailUtil();

        Thread t = new Thread(this);
        t.start();

    }


    /**
     *
     * @param context
     * @param valor
     * @param aviso
     */
    private void criarNotificacao(Context context, BigDecimal valor, String aviso) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivityDrawer.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.bittrexanalizer)
                .setTicker("Aviso BITTREXANALIZER")
                .setContentTitle("Aviso BITTREXANALIZER")
                .setContentText(descricao.toString() + aviso + valor.toString())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        Notification not = builder.build();
        not.flags = Notification.FLAG_AUTO_CANCEL;

        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        long milliseconds = 500;
        vibrator.vibrate(milliseconds);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, not);

    }

    @Override
    public void run() {
        try {

            tickers = new LinkedList<>();
            tickers = tickerDAO.findAllTickers();

            if (tickers.size() == 0) {
                return;
            }

            ExecutorService executorService = Executors.newCachedThreadPool();
            for (Ticker t : tickers) {
                ticker = t;
                ticker.setUrlApi(WebServiceUtil.getUrl() + ticker.getSigla().toLowerCase());
                executorService.execute(t);
            }

            executorService.shutdown();

            while (!executorService.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            verificarAviso();

        } catch (Exception e) {
            Log.i(LOG, e.getMessage());
            emailUtil.enviarEmail(context, e.getMessage(), "ERRO");
        }
    }

    private synchronized void verificarAviso() {
        try {

            for (Ticker ticker : tickers) {

                descricao = new StringBuilder();
                descricao.append(ticker.getSigla());
                descricao.append(" ESTA COM O ");


                if (!(ticker.getAsk().compareTo(ticker.getAvisoBuyInferior()) == 1) &&
                        ticker.getAvisoBuyInferior().compareTo(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_EVEN)) == 1) {
                    criarNotificacao(context, ticker.getAvisoBuyInferior(), "VALOR DE COMPRA ABAIXO DO ESPERADO ");
                }

                if (ticker.getAsk().compareTo(ticker.getAvisoBuySuperior()) == 1 &&
                        ticker.getAvisoBuySuperior().compareTo(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_EVEN)) == 1) {
                    criarNotificacao(context, ticker.getAvisoBuySuperior(), "VALOR DE COMPRA ACIMA DO ESPERADO ");

                }

                if (!(ticker.getBid().compareTo(ticker.getAvisoStopLoss()) == 1) &&
                        ticker.getAvisoStopLoss().compareTo(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_EVEN)) == 1) {
                    criarNotificacao(context, ticker.getAvisoStopLoss(), "VALOR DE VENDA ABAIXO DO ESPERADO ");
                }

                if (ticker.getBid().compareTo(ticker.getAvisoStopGain()) == 1 &&
                        ticker.getAvisoStopGain().compareTo(BigDecimal.ZERO.setScale(8, RoundingMode.HALF_EVEN)) == 1) {
                    criarNotificacao(context, ticker.getAvisoStopGain(), "VALOR DE VENDA ACIMA DO ESPERADO ");
                }
            }

            tickers = new LinkedList<>();

        } catch (Exception e) {
            Log.i(LOG, e.getMessage());
            emailUtil.enviarEmail(context, e.getMessage(), "ERRO");
        }
    }

}


package br.com.bittrexanalizer.facade;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.bittrexanalizer.analises.AnaliseRobot;
import br.com.bittrexanalizer.analises.IAnaliser;
import br.com.bittrexanalizer.database.dao.ConfiguracaoDAO;
import br.com.bittrexanalizer.database.dao.TickerDAO;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.domain.Configuracao;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.strategy.BalanceStrategy;
import br.com.bittrexanalizer.strategy.BuyOrderStrategy;
import br.com.bittrexanalizer.utils.CalculoUtil;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 16/09/2017.
 */

public class CompraFacade {


    private TickerDAO tickerDAO;
    private Context context;
    public static Map<String, LinkedList<Candle>> mapCandles;
    private boolean robotLigado = false;
    private boolean devoParar;
    private EmailUtil emailUtil;
    private AnaliseRobot analiseRobot;
    private BigDecimal valorParaCompraRobot;
    private final String BTC = "BTC";

    private Balance balance;
    private final String ROBOT_COMPRA = "COMPRA ROBOT ";
    private final String ROBOT_ERROS = "ERROS ROBOT ";


    private StringBuilder moedasErros = new StringBuilder();
    private LinkedList<Ticker> moedasHabilitadas;
    private volatile LinkedList<Ticker> tickers;
    private LinkedList<Ticker> tickersDoBD;

    private int qtdeTickersPesquisar;
    private int tempoEsperoThread;

    public void executar(Context context) {

        this.context = context;
        tickerDAO = new TickerDAO(context);

        getConfiguracoes();

        Log.i("Bittrex", "Entrei");

        //pegando as variaveis do Sistema
        robotLigado = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ROBOT_LIGADO));
        qtdeTickersPesquisar = Integer.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.QTDE_TICKERS_PESQUISA));
        tempoEsperoThread = Integer.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.TEMPO_ESPERA_THREAD));
        valorParaCompraRobot = new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.VALOR_COMPRA_ROBOT)).setScale(8);

        SessionUtil.getInstance().setMaxCandleParaPesquisar(0);

        //iniciando objetos
        analiseRobot = new AnaliseRobot();
        moedasHabilitadas = new LinkedList<>();
        emailUtil = new EmailUtil();

        moedasErros.append("\r\r"+ROBOT_ERROS);

        //Verifica se o robot esta ligado
        if (robotLigado) {
            try {
                executarRobot();
            } catch (Exception e) {
                moedasErros.append(e.getMessage() + e.getStackTrace());
            } finally {
                SessionUtil.getInstance().getMsgErros().append(moedasErros.toString());
            }
        }

    }

    private void getConfiguracoes() {

        LinkedList<Configuracao> configuracoes = new LinkedList<>();
        configuracoes = new ConfiguracaoDAO(context).all();

        Map<String, String> mapConfiguracao = new HashMap<String, String>();

        if (configuracoes == null) {
            SessionUtil.getInstance().setMapConfiguracao(null);
            return;
        }

        for (Configuracao c : configuracoes) {
            mapConfiguracao.put(c.getPropriedade(), c.getValor());
        }

        SessionUtil.getInstance().setMapConfiguracao(mapConfiguracao);


    }


    /**
     * Realiza o processamento para analisar os valores de todas as moedas
     */
    private void executarRobot() {

        //atualiza os valores de BTC
        BalanceStrategy.execute();

        //pega o valor da moeda BTC, utilizada para realizar a compra
        balance = SessionUtil.getInstance().getMapBalances().get(BTC);

        //valor disponivel em BTC é maior ou igual o valor minimo para compra?
        if (!temSaldoBTC()) {
            devoParar = true;
            moedasErros.append("SEM SALDO");
            return;
        }

        //pegando todos os tickers do Banco de Dados
        tickersDoBD = tickerDAO.findAllTickers();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                do {

                    mapCandles = new ConcurrentHashMap<>(new HashMap<String, LinkedList<Candle>>());

                    getDados();

                    Set<String> keys = null;
                    if (mapCandles != null) {
                        keys = mapCandles.keySet();
                    }


                    for (String k : keys) {

                        LinkedList<Candle> lista = mapCandles.get(k);
                        if (lista.size() > 0) {
                            realizarAnalises(k, lista);

                        }
                    }

                    try {
                        if (!devoParar) {
                            Thread.sleep(tempoEsperoThread);
                        }
                    } catch (InterruptedException e) {
                        moedasErros.append("- " + e.getMessage() + " - " + e.getStackTrace());
                        e.printStackTrace();
                    }

                } while (!devoParar);

                if (!moedasHabilitadas.isEmpty()) {
                    return;
                }


            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void getDados() {
        try {

            Boolean isAllTickers = false;
            /**
             * Faz a verificação se foi selecionados todas as moedas
             * ou se será calculado apenas nas moedas que o usuario esta analizando
             */
            if (SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ALL_TICKERS)) {

                isAllTickers = new Boolean(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ALL_TICKERS));

                if (isAllTickers) {
                    tickers = new LinkedList<>();
                    Set<String> keys = SessionUtil.getInstance().getNomeExchanges().keySet();

                    for (String k : keys) {
                        Ticker t = new Ticker();

                        t.setSigla(k);

                        tickers.add(t);
                    }
                } else {
                    tickers = SessionUtil.getInstance().getTickers();
                }

            } else {
                tickers = SessionUtil.getInstance().getTickers();
            }

            ExecutorService executorService = Executors.newCachedThreadPool();
            int flagParar = 0;
            int i = 0;


            if ((SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar) > tickers.size()) {
                i = SessionUtil.getInstance().getMaxCandleParaPesquisar();
                flagParar = tickers.size();
                SessionUtil.getInstance().setMaxCandleParaPesquisar(Integer.MIN_VALUE);
                devoParar = true;
            } else {
                flagParar = SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar;
                i = SessionUtil.getInstance().getMaxCandleParaPesquisar();
            }

            for (; i < tickers.size(); i++) {

                if (i == flagParar) {
                    SessionUtil.getInstance().setMaxCandleParaPesquisar(SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar);
                    break;
                }

                Candle candle = new Candle();
                candle.setSigla(tickers.get(i).getSigla());


                executorService.execute(candle);
            }

            executorService.shutdown();

            while (!executorService.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            moedasErros.append("Erro: " + e.getMessage());
            return;

        }
    }


    public void realizarAnalises(String sigla, LinkedList<Candle> candles) {

        boolean devoComprar = false;

        try {

            int valorOBV = analiseRobot.analizer(candles);

            if (valorOBV != IAnaliser.IDEAL_PARA_COMPRA) {
                devoComprar = false;
            } else {
                devoComprar = true;
            }


            if (devoComprar) {

                BalanceStrategy.execute();

                //atualiza o Balance
                balance = SessionUtil.getInstance().getMapBalances().get(BTC);

                //tem saldo?
                if (!temSaldoBTC()) {
                    devoParar = true;
                    return;
                }

                Ticker t = new Ticker();
                t.setSigla(sigla);
                t.setUrlApi(WebServiceUtil.getUrl() + t.getSigla().toLowerCase());

                t = localizarValorDoTicker(t);

                //realiza a compra
                boolean comprado = comprar(t);

                if (comprado) {

                    //calcula porcentagem
                    t.setAvisoStopLoss(CalculoUtil.getPorcentagemLoss(t.getBid()));
                    t.setAvisoStopGain(CalculoUtil.getPorcentagemLimit(t.getBid()));
                    t.setBought(true);

                    boolean jaExistia = false;

                    for (Ticker temp : tickersDoBD) {
                        //já existe?
                        if (temp.getSigla().toLowerCase().equals(t.getSigla().toLowerCase())) {
                            t.setId(temp.getId());
                            t.setNomeExchange(SessionUtil.getInstance().getNomeExchanges().get(t.getSigla()));
                            //atualizado
                            tickerDAO.update(t);
                            jaExistia = true;

                            continue;
                        }
                    }

                    //não existia
                    if (!jaExistia) {
                        t.setNomeExchange(SessionUtil.getInstance().getNomeExchanges().get(t.getSigla()));

                        tickerDAO.create(t);
                    }
                    String mensagem = ROBOT_COMPRA + "Moeda: " + t.getSigla();
                    mensagem += "\n - Valor: " + t.getAsk();
                    mensagem += "\n - Valor Limit: " + t.getAvisoStopGain();
                    mensagem += "\n - Valor Lost: " + t.getAvisoStopLoss();
                    enviarEmail(mensagem, ROBOT_COMPRA);

                    moedasHabilitadas.add(t);
                }

            }

        } catch (Exception e) {
            moedasErros.append("\t\r");
            moedasErros.append(e.getMessage());
        }

    }

    private Ticker localizarValorDoTicker(Ticker tic) {

        LinkedList<Ticker> tickersTemp = new LinkedList<>();
        tickersTemp.add(tic);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Ticker t : tickersTemp) {
            executorService.execute(t);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return tickersTemp.getFirst();

    }

    private boolean comprar(Ticker ticker) {
        Order order = new Order();
        order.setSigla(ticker.getSigla());

        //Calcula a quantidade da moeda que será comprada
        order.setQuantity(CalculoUtil.getQuantidadeASerComprada(valorParaCompraRobot, ticker.getAsk()));

        //Pega o valor atual de compra da moeda
        order.setRate(ticker.getAsk());

        boolean retorno = false;

        Log.i("Bittrex", "Comprar: " + ticker.getSigla() + " - Valor Venda: " + ticker.getAvisoStopGain() + " - Lost: " + ticker.getAvisoStopLoss());

        //executa a compra
        retorno = BuyOrderStrategy.execute(order);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return retorno;

    }

    private boolean temSaldoBTC() {

        if (balance.getBalance().compareTo(valorParaCompraRobot) == -1) {
            moedasErros.append("SEM SALDO");
            return false;
        }

        return true;

    }

    private void enviarEmail(final String mensagem, final String operacao) {

        emailUtil.enviarEmail(context, "COMPRA ROBOT: ", mensagem, operacao);

    }

}


package br.com.bittrexanalizer.utils;

import org.apache.commons.codec.Charsets;

import java.nio.charset.Charset;

/**
 * Created by PauLinHo on 19/01/2018.
 */

public class HexUtil {

    public static final Charset DEFAULT_CHARSET;
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final char[] DIGITS_LOWER;
    private static final char[] DIGITS_UPPER;

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase?DIGITS_LOWER:DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for(int var5 = 0; i < l; ++i) {
            out[var5++] = toDigits[(240 & data[i]) >>> 4];
            out[var5++] = toDigits[15 & data[i]];
        }

        return out;
    }

    static {
        DEFAULT_CHARSET = Charsets.UTF_8;
        DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }

}


package br.com.bittrexanalizer.utils;

/**
 * Created by PauLinHo on 22/01/2018.
 */

public class ConstantesUtil {

    public static final String LONG_EMA = "LONG_EMA";
    public static final String SHORT_EMA = "SHORT_EMA";
    public static final String SIGNAL = "SIGNAL";
    public static final String EMAIL = "EMAIL";
    public static final String IFR_DIAS = "IFR_DIAS";
    public static final String IFR_MAX = "IFR_MAX";
    public static final String IFR_MIN = "IFR_MIN";
    public static final String ALL_TICKERS = "ALL_TICKERS";
    public static final String TEMPO_ANALISE_MIN = "TEMPO_ANALISE_MIN";
    public static final String MACD = "MACD";
    public static final String IFR = "IFR";
    /**
     * Valor em porcentagem do STOP LOSS ex.95
     */
    public static final String STOP_LOSS = "STOP_LOSS";
    public static final String STOP_GAIN = "STOP_GAIN";
    public static final String PERIODICIDADE = "PERIODICIDADE";
    public static final String ENVIAR_EMAIL = "ENVIAR_EMAIL";
    public static final String CELULAR = "CELULAR";
    public static final String ENVIAR_SMS = "ENVIAR_SMS";
    public static final String ENVIAR_NOTIFICACAO = "ENVIAR_NOTIFICACAO";

    public static final String OSCILADOR_ESTOCASTICO = "OSCILADOR_ESTOCASTICO" ;
    public static final String OE_TEMPO_PERIODO_K = "OE_TEMPO_PERIODO_K";
    public static final String OE_TEMPO_PERIODO_D = "OE_TEMPO_PERIODO_D";
    public static final String OE_TAXA_MIN = "OE_TAXA_MIN";
    public static final String OE_TAXA_MAX = "OE_TAXA_MAX";
    public static final String QTDE_TICKERS_PESQUISA = "QTDE_TICKERS_PESQUISA";
    public static final String TEMPO_ESPERA_THREAD = "TEMPO_ESPERA_THREAD";
    public static final String OBV = "OBV";
    public static final String OBV_QTDE_FECHAMENTOS = "OBV_QTDE_FECHAMENTOS";

    public static final String CANCEl_ORDER = "BittrexAnalizer - CANCEL ORDER";
    public static final String COMPRA_REALIZADA = "BittrexAnalizer - COMPRA REALIZADA";
    public static final String VENDA_REALIZADA = "BittrexAnalizer - VENDA REALIZADA";
    public static final String STOP_CRIADO = "BittrexAnalizer - STOP CRIADO";
    public static final String ANALISE_LIGADA = "ANALISE_LIGADA";
    public static final String ROBOT_VEZES = "ROBOT_VEZES";
    public static final String ROBOT_LIGADO = "ROBOT_LIGADO";
    public static final String VALOR_COMPRA_ROBOT = "VALOR_COMPRA_ROBOT";
    public static final String ROBOT_TEMPO_ENVIO_EMAIL = "ROBOT_TEMPO_ENVIO_EMAIL";
}


package br.com.bittrexanalizer.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by PauLinHo on 27/07/2017.
 */

/**
 * Strategy verifica a conexão com a internet
 */
public class VerificaConexaoStrategy {

    /**
     * Verifica se há conexão com a internet
     * @param context
     * @return - true se houver conexão
     */
    public static boolean verificarConexao(Context context) {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

}


package br.com.bittrexanalizer.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Ticker;


/**
 * Created by PauLinHo on 30/09/2017.
 */

public class TickerComparator  {

    public static LinkedList<Ticker> ordenar(String criterio, LinkedList<Ticker> listaDesordenada){

        switch (criterio) {

            case "NOME ASC":
                Collections.sort(listaDesordenada, getNomeAsc());
                break;

            case "NOME DESC":
                Collections.sort(listaDesordenada, getNomeDesc());
                break;

            case "LAST ASC":
                Collections.sort(listaDesordenada, getLastAsc());
                break;

            case "LAST DESC":
                Collections.sort(listaDesordenada, getLastDesc());
                break;

        }

        return listaDesordenada;

    }



    private static Comparator<Ticker> getNomeAsc() {
        return new Comparator<Ticker>() {

            @Override
            public int compare(Ticker o1, Ticker o2) {
                int valor = o2.getNomeExchange().compareTo(o1.getNomeExchange()) * -1;
                // se for igual, comparar por sigla
                if (valor == 0) {
                    return o2.getSigla().compareTo(o1.getSigla());
                }
                return valor;
            }
        };
    }

    private static Comparator<Ticker> getNomeDesc() {
        return new Comparator<Ticker>() {

            @Override
            public int compare(Ticker o1, Ticker o2) {
                int valor = o1.getNomeExchange().compareTo(o2.getNomeExchange()) * -1;
                // se for igual, comparar por sigla
                if (valor == 0) {
                    return o1.getSigla().compareTo(o2.getSigla());
                }
                return valor;
            }
        };
    }

    private static Comparator<Ticker> getLastAsc() {
        return new Comparator<Ticker>() {
            @Override
            public int compare(Ticker o1, Ticker o2) {
                int valor = o2.getLast().compareTo(o1.getLast()) * -1;
                // se for igual, comparar por Nome
                if (valor == 0) {
                    return o2.getNomeExchange().compareTo(o1.getNomeExchange());
                }
                return valor;
            }
        };
    }

    private static Comparator<Ticker> getLastDesc() {
        return new Comparator<Ticker>() {
            @Override
            public int compare(Ticker o1, Ticker o2) {
                int valor = o1.getLast().compareTo(o2.getLast()) * -1;
                // se for igual, comparar por Nome
                if (valor == 0) {
                    return o1.getNomeExchange().compareTo(o2.getNomeExchange());
                }
                return valor;
            }
        };
    }

}


package br.com.bittrexanalizer.utils;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PauLinHo on 10/09/2017.
 */

public class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private SimpleDateFormat JSON_STRING_DATE;

    public DateDeserializer(SimpleDateFormat sdf){
        this.JSON_STRING_DATE = sdf;
    }

    @Override
    public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {

        try {
            return JSON_STRING_DATE.parse(je.getAsString());
        } catch (ParseException ex) {
            Log.i("BITTREX", ex.getMessage());
        }
        return null;
    }

    @Override
    public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc) {

        String data = JSON_STRING_DATE.format(t);

        return new JsonPrimitive(data);
    }
}


package br.com.bittrexanalizer.utils;

import android.content.Context;

/**
 * Created by PauLinHo on 24/01/2018.
 */

public class SMSUtil {

    private Context context;
    private String operacao;

//    public void enviarSMS(Paciente paciente) {
//
//        String texto = gerarTextSMS(paciente);
//
//        String numero = paciente.getTelefones().get(0).getNumero().replace("(","").replace(")","");
//
//
//
//    }

//    public boolean enviarSMS(Context context, String mensagem, String operacao) {
//
//        this.context = context;
//
//        this.operacao = operacao;
//
//        String assunto = "Aviso de Moeda dentro dos limites técnicos para " + operacao;
//
//        String texto = gerarTextoEmail(mensagem);
//
//        String email = "";
//
//        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.EMAIL)) {
//            Toast.makeText(context, "Não foi localizado a propriedade EMAIL em configurações", Toast.LENGTH_LONG).show();
//            return false;
//        } else {
//            email = SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.EMAIL).toLowerCase();
//        }
//
//
//        try {
//            ActivityCompat.requestPermissions(MainActivityDrawer.class, new String[]{Manifest.permission.SEND_SMS}, 1);
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(numero, null, texto, null, null);
//
//        } catch (Exception e) {
//            Log.i("REGULAMOGI", e.getMessage());
//        }
//        return true;
//    }
//
//    private String gerarTextoEmail(String mensagem) {
//
//        StringBuilder texto = new StringBuilder();
//        texto.append("BITTREX ANALIZER ");
//        texto.append("\n\n");
//        texto.append(mensagem);
//
//        return texto.toString();
//
//    }

}


package br.com.bittrexanalizer.utils;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import br.com.bittrexanalizer.api.ApiCredentials;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.domain.Ticker;

/**
 * Created by PauLinHo on 13/08/2017.
 */

public class SessionUtil {

    private static SessionUtil instance = new SessionUtil();
    private Ticker ticker;
    private LinkedList<Ticker> tickers;
    private ApiCredentials apiCredentials;
    private Map<String, String> mapConfiguracao;
    private Map<String, String> nomeExchanges;
    private long ultimoTempoDeNotificacaoSalvo = 0;
    private int maxCandleParaPesquisar = 0;
    private Map<String, Order> mapOpenOrders;
    private Map<String, Balance> mapBalances;
    private StringBuilder msgErros;

    private long ultimoHorarioSalvo;

    private SessionUtil() {
        setApiCredentials(new ApiCredentials());

        setMapConfiguracao(new HashMap<String, String>());
        setMapOpenOrders(new HashMap<String, Order>());
        setMapBalances(new HashMap<String, Balance>());

        nomeExchanges = new HashMap<String, String>();

        msgErros = new StringBuilder();

        ticker = new Ticker();
        tickers = new LinkedList<>();
    }

    public static SessionUtil getInstance() {
        return instance;
    }





    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public LinkedList<Ticker> getTickers() {
        return tickers;
    }

    public void setTickers(LinkedList<Ticker> tickers) {
        this.tickers = tickers;
    }

    public ApiCredentials getApiCredentials() {
        return apiCredentials;
    }

    public void setApiCredentials(ApiCredentials apiCredentials) {
        this.apiCredentials = apiCredentials;
    }

    public Map<String, String> getMapConfiguracao() {
        return mapConfiguracao;
    }

    public void setMapConfiguracao(Map<String, String> mapConfiguracao) {
        this.mapConfiguracao = mapConfiguracao;
    }

    public Map<String, String> getNomeExchanges() {
        return nomeExchanges;
    }

    public void setNomeExchanges(Map<String, String> nomeExchanges) {
        this.nomeExchanges = nomeExchanges;
    }

    public long getUltimoTempoDeNotificacaoSalvo() {
        return ultimoTempoDeNotificacaoSalvo;
    }

    public void setUltimoTempoDeNotificacaoSalvo(long ultimoTempoDeNotificacaoSalvo) {
        this.ultimoTempoDeNotificacaoSalvo = ultimoTempoDeNotificacaoSalvo;
    }

    public int getMaxCandleParaPesquisar() {
        return maxCandleParaPesquisar;
    }

    public void setMaxCandleParaPesquisar(int maxCandleParaPesquisar) {
        this.maxCandleParaPesquisar = maxCandleParaPesquisar;
    }

    public Map<String, Order> getMapOpenOrders() {
        return mapOpenOrders;
    }

    public void setMapOpenOrders(Map<String, Order> mapOpenOrders) {
        this.mapOpenOrders = mapOpenOrders;
    }

    public Map<String, Balance> getMapBalances() {
        return mapBalances;
    }

    public void setMapBalances(Map<String, Balance> mapBalances) {
        this.mapBalances = mapBalances;
    }

    public StringBuilder getMsgErros() {
        return msgErros;
    }

    public void setMsgErros(StringBuilder msgErros) {
        this.msgErros = msgErros;
    }

    public long getUltimoHorarioSalvo() {
        return ultimoHorarioSalvo;
    }

    public void setUltimoHorarioSalvo(long ultimoHorarioSalvo) {
        this.ultimoHorarioSalvo = ultimoHorarioSalvo;
    }
}


package br.com.bittrexanalizer.utils;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.OrderHistory;

/**
 * Created by PauLinHo on 10/09/2017.
 */

public class WebServiceUtil {

    private static final String URL = "https://bittrex.com/api/v1.1/public/getticker?market=btc-";
    private static final String URL_BALANCE_APY_KEY = "https://bittrex.com/api/v1.1/account/getbalances?apikey=";
    private static final String URL_OPEN_ORDERS = "https://bittrex.com/api/v1.1/market/getopenorders?apikey=";
    private static final String URL_ORDER = "https://bittrex.com/api/v1.1/account/getorder&uuid=";
    private static final String URL_TICKS = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-";
    private static final String URL_ORDER_HISTORY = "https://bittrex.com/api/v1.1/account/getorderhistory";

    private static final String URL_TRADE_BUY_V1 = "https://bittrex.com/api/v1.1/market/buylimit?apikey=";
    private static final String URL_TRADE_SELL_V1 = "https://bittrex.com/api/v1.1/market/selllimit?apikey=";
    private static final String URL_ORDER_CANCEL = "https://bittrex.com/api/v1.1/market/cancel?apikey=";

    //V2.0
    private static final String URL_TRADE_BUY = "https://bittrex.com/api/v2.0/auth/market/TradeBuy";
    private static final String URL_TRADE_SELL = "https://bittrex.com/api/v2.0/auth/market/TradSell";
    private static final String URL_ORDER_HISTORY_V_20 = "https://bittrex.com/Api/v2.0/auth/orders/GetOrderHistory";

    private static final String TICKINTERVAL_ONE_MIN = "oneMin";
    private static final String TICKINTERVAL_FIVE_MIN = "fiveMin";
    private static final String TICKINTERVAL_THIRTY_MIN = "thirtyMin";
    private static final String TICKINTERVAL_HOUR = "hour";
    private static final String TICKINTERVAL_DAY = "day";


    public static String getUrl() {
        return getURL();
    }

    /**
     * Verifica se a dados devolvido do web service foi true ou false
     *
     * @param dados
     * @return
     */
    public static boolean verificarRetorno(String dados) {
        boolean retorno = false;

        String[] split = dados.replace("{", "")
                .replace("}","")
                .replace(":","")
                .replace(",","")
                .split("\"");

        for(int i = 0; i<split.length-1;i++){
            if(split[i].equals("success")){
                retorno = new Boolean(split[i+1]);
                break;
            }
        }

        return retorno;

    }

    /**
     * Ex. https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-CVC&tickInterval=thirtyMin&_=1500915289433
     *
     * @param URL               URL BASE
     * @param marketName:string - SIGLA DA MOEDA
     * @param tickInterval      [“oneMin”, “fiveMin”, “thirtyMin”, “hour”, “day”]
     * @return
     */
    public static String construirURLTicker(String URL, String marketName, String tickInterval) {


        StringBuilder uri = new StringBuilder();

        uri.append(URL);
        uri.append(marketName);
        uri.append("&tickInterval=");
        uri.append(tickInterval);
        uri.append("&_=");
        uri.append(System.currentTimeMillis());


        return uri.toString();

    }

    public static String construirURLTickerBUYv1(String marketName, String quantity, String rate) {

        //API_KEY&market=BTC-LTC&quantity=1.2&rate=1.3";

        StringBuilder uri = new StringBuilder();

        uri.append(getUrlTradeBuyV1());
        uri.append(SessionUtil.getInstance().getApiCredentials().getKey());
        uri.append("&");
        uri.append("nonce");
        uri.append("=");
        uri.append(EncryptionUtility.generateNonce());
        uri.append("&market=BTC-");
        uri.append(marketName);
        uri.append("&quantity=");
        uri.append(quantity);
        uri.append("&rate=");
        uri.append(rate);


        return uri.toString();

    }

    public static String construirURLTickerSellv1(String marketName, String quantity, String rate) {

        //API_KEY&market=BTC-LTC&quantity=1.2&rate=1.3";

        StringBuilder uri = new StringBuilder();

        uri.append(getUrlTradeSellV1());
        uri.append(SessionUtil.getInstance().getApiCredentials().getKey());
        uri.append("&");
        uri.append("nonce");
        uri.append("=");
        uri.append(EncryptionUtility.generateNonce());
        uri.append("&market=BTC-");
        uri.append(marketName);
        uri.append("&quantity=");
        uri.append(quantity);
        uri.append("&rate=");
        uri.append(rate);


        return uri.toString();

    }


    public static String constuirgetUrlOrderHistoryV2_0() {

        //                MarketName:string, OrderType:string, Quantity:float, Rate:float,
//                TimeInEffect:string,ConditionType:string, Target:int __RequestVerificationToken:string

        // POST https://bittrex.com/api/v2.0/auth/market/TradeBuy with data { MarketName: "BTC-DGB,
        // OrderType:"LIMIT", Quantity: 10000.02, Rate: 0.0000004, TimeInEffect:"GOOD_TIL_CANCELED",
        // ConditionType: "NONE", Target: 0, __RequestVerificationToken: "HIDDEN_FOR_PRIVACY"}

        OrderHistory orderHistory = new OrderHistory();
        orderHistory.set_RequestVerificationToken(SessionUtil.getInstance().getApiCredentials().getKey());

        StringBuilder uri = new StringBuilder();
        uri.append(getUrlOrderHistoryV20());
        uri.append("?apikey=");
        uri.append(SessionUtil.getInstance().getApiCredentials().getKey());
        uri.append("&");
        uri.append("nonce");
        uri.append("=");
        uri.append(EncryptionUtility.generateNonce());

//        uri.append("&market=BTC-");
//        uri.append(order.getExchange());
//        uri.append("&quantity=");
//        uri.append(order.getQuantity());
//        uri.append("&OrderType=");
//        uri.append(order.getOrderType());
//        uri.append("&rate=");
//        uri.append(order.getRate());


        return uri.toString();

    }



    public static String addNonce(String URL) {

        if (SessionUtil.getInstance().getApiCredentials() == null) {
            return "";
        }

        StringBuilder uri = new StringBuilder();

        uri.append(URL);
        uri.append(SessionUtil.getInstance().getApiCredentials().getKey());
        uri.append("&");
        uri.append("nonce");
        uri.append("=");
        uri.append(EncryptionUtility.generateNonce());


        return uri.toString();

    }

    public static String addNonce(String URL, String UUID) {

        if (SessionUtil.getInstance().getApiCredentials() == null) {
            return "";
        }

        StringBuilder uri = new StringBuilder();

        uri.append(URL);
        uri.append(SessionUtil.getInstance().getApiCredentials().getKey());
        uri.append("&");
        uri.append("nonce");
        uri.append("=");
        uri.append(EncryptionUtility.generateNonce());
        uri.append("&uuid=");
        uri.append(UUID);


        return uri.toString();

    }


    public static String getURL() {
        return URL;
    }

    public static String getUrlBalanceApyKey() {
        return URL_BALANCE_APY_KEY;
    }

    public static String getUrlOpenOrders() {
        return URL_OPEN_ORDERS;
    }

    public static String getUrlOrder() {
        return URL_ORDER;
    }

    public static String getUrlTicks() {
        return URL_TICKS;
    }

    public static String getTickintervalOneMin() {
        return TICKINTERVAL_ONE_MIN;
    }

    public static String getTickintervalFiveMin() {
        return TICKINTERVAL_FIVE_MIN;
    }

    public static String getTickintervalThirtyMin() {
        return TICKINTERVAL_THIRTY_MIN;
    }

    public static String getTickintervalHour() {
        return TICKINTERVAL_HOUR;
    }

    public static String getTickintervalDay() {
        return TICKINTERVAL_DAY;
    }

    public static String getUrlOrderHistory() {
        return URL_ORDER_HISTORY;
    }

    public static String getUrlTradeBuyV1() {
        return URL_TRADE_BUY_V1;
    }

    /**
     * { MarketName: "BTC-DGB,
     * OrderType:"LIMIT",
     * Quantity: 10000.02,
     * Rate: 0.0000004,
     * TimeInEffect:"GOOD_TIL_CANCELED",
     * ConditionType: "NONE",
     * Target: 0,
     * __RequestVerificationToken:
     * "HIDDEN_FOR_PRIVACY"}
     */
    public static String getUrlTradeBuy() {
        return URL_TRADE_BUY;
    }

    /**
     * { MarketName: "BTC-DGB,
     * OrderType:"LIMIT",
     * Quantity: 10000.02,
     * Rate: 0.0000004,
     * TimeInEffect:"GOOD_TIL_CANCELED",
     * ConditionType: "NONE",
     * Target: 0,
     * __RequestVerificationToken: "HIDDEN_FOR_PRIVACY"}
     */
    public static String getUrlTradeSell() {
        return URL_TRADE_SELL;
    }

    public static String getUrlOrderCancel() {
        return URL_ORDER_CANCEL;
    }

    public static String getUrlTradeSellV1() {
        return URL_TRADE_SELL_V1;
    }

    public static String getUrlOrderHistoryV20() {
        return URL_ORDER_HISTORY_V_20;
    }
}

package br.com.bittrexanalizer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by PauLinHo on 21/01/2018.
 */

public class EmailUtil {

    private String operacao = "";
    private Context context;
    private String assunto = "";

    public boolean enviarEmail(final Context context, String mensagem, String operacao) {

        this.context = context;

        this.operacao = operacao;

        if(assunto.length() ==0) {
            assunto = "BITRREX " + operacao;
        }

        final String texto = gerarTextoEmail(mensagem);

        String email = "";

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.EMAIL)) {
            Toast.makeText(context, "Não foi localizado a propriedade EMAIL em configurações", Toast.LENGTH_LONG).show();
            return false;
        } else {
            email = SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.EMAIL).toLowerCase();
        }


        final String finalEmail = email;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                CommonsEmailSend commonsEmailSend = new CommonsEmailSend(context,
                        finalEmail,
                        assunto,
                        texto);
                commonsEmailSend.sendMail();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean enviarEmail(Context context, String assunto, String mensagem, String operacao) {

        this.assunto = assunto;

        return enviarEmail(context, mensagem, operacao);
    }

    private String gerarTextoEmail(String mensagem) {

        StringBuilder texto = new StringBuilder();
        texto.append("BITTREX ANALIZER ");
        texto.append("\n\n");
        texto.append(mensagem);

        return texto.toString();

    }
}


package br.com.bittrexanalizer.utils;

import java.math.BigDecimal;

/**
 * Created by PauLinHo on 16/09/2017.
 */

public class DecimalFormatUtil {

    public static BigDecimal getBigDecimalFormatado(BigDecimal valor){

        BigDecimal numFormatado = valor.setScale(2, BigDecimal.ROUND_UP);

        return numFormatado;
    }

}


package br.com.bittrexanalizer.utils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


/**
 * Created by PauLinHo on 30/09/2017.
 */

public class BigDecimalComparator {

    public static LinkedList<BigDecimal> ordenar(String criterio, LinkedList<BigDecimal> listaDesordenada){

        switch (criterio) {

            case "CLOSE ASC":
                Collections.sort(listaDesordenada, getCloseAsc());
                break;

            case "LOW ASC":
                Collections.sort(listaDesordenada, getLowAsc());
                break;

            case "HIGH ASC":
                Collections.sort(listaDesordenada, getHighAsc());
                break;


        }

        return listaDesordenada;

    }



    private static Comparator<BigDecimal> getCloseAsc() {
        return new Comparator<BigDecimal>() {

            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                int valor = o2.compareTo(o1) * -1;
                return valor;
            }
        };
    }

    private static Comparator<BigDecimal> getHighAsc() {
        return new Comparator<BigDecimal>() {

            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                int valor = o2.compareTo(o1) * -1;
                return valor;
            }
        };
    }

    private static Comparator<BigDecimal> getLowAsc() {
        return new Comparator<BigDecimal>() {

            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                int valor = o2.compareTo(o1) * -1;
                return valor;
            }
        };
    }



}


package br.com.bittrexanalizer.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by PauLinHo on 28/09/2017.
 */

public class CommonsEmailSend {

    private Session session;
    private Context context;
    private String PASSWORD = "bittrexanalizer07";
    private String rec;
    private String subject;
    private String textMessage;
    private final String EMAIL_SERVIDOR = "bittrexanalizer@omniatechnology.com.br";


    public CommonsEmailSend(Context context, String rec, String subject, String textMessage) {
        this.context = context;
        this.rec = rec;
        this.subject = subject;
        this.textMessage = textMessage;
    }

    public void sendMail() {

        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.omniatechnology.com.br");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SERVIDOR, PASSWORD);
            }
        });


        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    Message message = new MimeMessage(session);
                    try {
                        message.setFrom(new InternetAddress(EMAIL_SERVIDOR));

                        message.setRecipients(RecipientType.TO, InternetAddress.parse(rec));
                        message.setSubject(subject);
                        message.setContent(textMessage, "text/html; charset=utf-8");

                        Transport.send(message);

                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }

                }
            });
            t.start();
        } catch (Exception e) {


        }


        class RetrieveFeedTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                try {

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EMAIL_SERVIDOR));
                    message.setRecipients(RecipientType.TO, InternetAddress.parse(rec));
                    message.setSubject(subject);
                    message.setContent(textMessage, "text/html; charset=utf-8");

                    Transport.send(message);


                } catch (MessagingException m) {

                } catch (Exception e) {

                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {

                Toast.makeText(context, "Mensagem enviada com sucesso!", Toast.LENGTH_LONG).show();
            }

        }
    }
}



package br.com.bittrexanalizer.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Configuracao;


/**
 * Created by PauLinHo on 30/09/2017.
 */

public class ConfiguracaoComparator {

    public static final String PROP_ASC = "PROPRIEDADE ASC";
    public static final String PROP_DESC = "PROPRIEDADE DESC";
    public static final String VALOR_ASC = "VALOR ASC";

    public static LinkedList<Configuracao> ordenar(String criterio, LinkedList<Configuracao> listaDesordenada){

        switch (criterio) {

            case "PROPRIEDADE ASC":
                Collections.sort(listaDesordenada, getPropriedadeASC());
                break;

            case "PROPRIEDADE DESC":
                Collections.sort(listaDesordenada, getPropriedadeDESC());
                break;

            case "VALOR ASC":
                Collections.sort(listaDesordenada, getValorASC());
                break;


        }

        return listaDesordenada;

    }



    private static Comparator<Configuracao> getPropriedadeASC() {
        return new Comparator<Configuracao>() {

            @Override
            public int compare(Configuracao o1, Configuracao o2) {
                int valor = o2.getPropriedade().compareTo(o1.getPropriedade()) * -1;
                return valor;
            }
        };
    }

    private static Comparator<Configuracao> getPropriedadeDESC() {
        return new Comparator<Configuracao>() {

            @Override
            public int compare(Configuracao o1, Configuracao o2) {
                int valor = o1.getPropriedade().compareTo(o2.getPropriedade()) * -1;
                return valor;
            }
        };
    }

    private static Comparator<Configuracao> getValorASC() {
        return new Comparator<Configuracao>() {

            @Override
            public int compare(Configuracao o1, Configuracao o2) {
                int valor = o2.getValor().compareTo(o1.getValor()) * -1;
                return valor;
            }
        };
    }



}


package br.com.bittrexanalizer.utils;

import java.math.BigDecimal;

/**
 * Created by PauLinHo on 04/02/2018.
 */

public class CalculoUtil {

    private static final Double TAXA = 0.0025;

    public static BigDecimal getPorcentagemLoss(BigDecimal valorAplicado){

        BigDecimal porcentagem = new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.STOP_LOSS));

        return getPorcentagem(valorAplicado, porcentagem);
    }

    public static BigDecimal getPorcentagemLimit(BigDecimal valorAplicado){

        BigDecimal porcentagem = new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.STOP_GAIN));

        return getPorcentagem(valorAplicado, porcentagem);
    }

    /**
     * Calcula a porcentagem, baseada no valor descontado pelo BITTREX
     * @param valorAplicado - Valor em BigDecimal aplicado
     * @param porcentagem - Porcentagem esperada
     * @return - Resultado da operacao
     */
    public static BigDecimal getPorcentagem(BigDecimal valorAplicado, BigDecimal porcentagem){

        valorAplicado = valorAplicado.add(valorAplicado.multiply(new BigDecimal(TAXA)));

        BigDecimal aux = valorAplicado.multiply(porcentagem).setScale(8, BigDecimal.ROUND_HALF_EVEN);
        aux = aux.divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_EVEN);

        return aux;

    }

    /**
     * Calcula a quantidade em moeda que será obtido comprando em determinado valor,
     * por determinada quantia de BTC
     * @param quantidadeBTC - quantidade em BTC
     * @param valorMoeda - Valor da Moeda a ser Comprada
     * @return - Um BigDecimal com a quantidade
     */
    public static BigDecimal getQuantidadeASerComprada(BigDecimal quantidadeBTC, BigDecimal valorMoeda){

        BigDecimal resultado = new BigDecimal("0").setScale(8);

        //retira do quantidade de BTC a taxa
        quantidadeBTC = quantidadeBTC.subtract(
                            (quantidadeBTC.multiply(new BigDecimal(TAXA)))
                                    .setScale(8, BigDecimal.ROUND_HALF_EVEN));

        //divide pelo valor da moeda
        resultado = quantidadeBTC.divide(valorMoeda,BigDecimal.ROUND_HALF_EVEN);


        return resultado;

    }



}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterOrder;
import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.strategy.OrderStrategy;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class UserOrders extends Activity implements IFlagment {

    private ListView lstOrders;
    private LinkedList<Order> orders;
    private AdapterOrder adapterOrders;
    private SwipeRefreshLayout swipeRefreshMain;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        lstOrders = findViewById(R.id.lstOrders);
        swipeRefreshMain = findViewById(R.id.swipeRefleshMain);

        swipeRefreshMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrders();
                swipeRefreshMain.setRefreshing(false);
            }
        });

        swipeRefreshMain.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        );

        getOrders();

        lstOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              Order order = (Order) lstOrders.getItemAtPosition(position);

              if(order!=null) {
                  Intent i = new Intent(UserOrders.this, UserOrder.class);
                  Bundle b =  new Bundle();
                  b.putSerializable("ORDER", order);
                  i.putExtras(b);
                  startActivity(i);
              }else{
                  Toast.makeText(UserOrders.this, "Erro ao Abrir a Order", Toast.LENGTH_SHORT).show();
              }

            }
        });

    }

    private void getOrders() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                String url = WebServiceUtil.getUrlOrderHistory();

                if (url.length() < 1) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserOrders.this, "Ocorreu um erro. Verifique se existem Chaves cadastradas", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, "HmacSHA512");

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

                if (!WebServiceUtil.verificarRetorno(dados)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserOrders.this, "Ocorreu um erro", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    orders = new OrderStrategy().getObjects(dados);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (orders.size() == 0) {
                            Toast.makeText(UserOrders.this, "Sem dados para exibição", Toast.LENGTH_LONG).show();
                        } else {
                            atualizarListView();
                        }

                    }
                });
            }
        });
        t.start();


    }

    @Override
    public void atualizarListView() {

        if (adapterOrders == null) {
            adapterOrders = new AdapterOrder(UserOrders.this, orders);
            lstOrders.setAdapter(adapterOrders);
        } else {
            adapterOrders.clear();
            adapterOrders.notifyDataSetChanged();
            adapterOrders.addAll(orders);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterKeys;
import br.com.bittrexanalizer.api.ApiCredentials;
import br.com.bittrexanalizer.database.dao.ApiCredentialsDAO;

/**
 * Created by PauLinHo on 16/01/2018.
 */

public class UserKey extends Activity implements IFlagment {


    private ApiCredentials apiCredentials;
    private ListView lstApiCredentials;
    private AdapterKeys adapterApiCredentials;
    private LinkedList<ApiCredentials> listaApiCredentials;

    private ImageButton imgButtonNewCrud;
    private ImageButton imgButtonDeleteCrud;
    private ImageButton imgButtonEditCrud;

    private boolean foiPersistido = false;
    private String operacaoEscolhida = "";

    private LayoutInflater inflater;
    private AlertDialog alert;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;

    private ApiCredentialsDAO apiCredentialsDAO;

    private EditText inpApiCredentialsKey;
    private EditText inpApiCredentialsSecret;

    //variables bundle
    private boolean flagIsStateSaved = false;
    private String apiCredentialsSecretSaved;
    private String apiCredentialsKeySaved;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_keys);

        if (savedInstanceState != null) {
            flagIsStateSaved = true;
            apiCredentialsSecretSaved = savedInstanceState.getString("apiCredentialsSecretSaved");
            apiCredentialsKeySaved = savedInstanceState.getString("apiCredentialsKeySaved");
        }

        lstApiCredentials = findViewById(R.id.lstApiCredentials);
        apiCredentials = new ApiCredentials();

        FloatingActionButton fab = findViewById(R.id.fabKey);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiCredentials = new ApiCredentials();
                criarDialogCRUD(IFlagment.CADASTRAR);
            }
        });


        apiCredentialsDAO = new ApiCredentialsDAO(UserKey.this);

        atualizarListView();

        lstApiCredentials.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                apiCredentials = new ApiCredentials();
                apiCredentials = (ApiCredentials) lstApiCredentials.getItemAtPosition(i);

                showDialog();

                return true;
            }
        });

    }

    /**
     * Exibe o dialog para escolher a opçao de CRUD
     * Variavel que indica se foi clicado de do fat(TRUE) ou do ListView(false)
     * Se foi clicado do Flag nao ativa a opçao de exclusao e delete
     */
    private void showDialog() {

        final AlertDialog.Builder dialogCrud = new AlertDialog.Builder(UserKey.this);

        inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_crud, null);
        dialogCrud.setView(dialogView);
        dialogCrud.setTitle(IFlagment.MENSAGEM_DIALOG);

        final View.OnClickListener criarDialog = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imgButtonNewCrud.getId() == view.getId()) {
                    apiCredentials = new ApiCredentials();
                    criarDialogCRUD(IFlagment.CADASTRAR);
                } else if (imgButtonDeleteCrud.getId() == view.getId()) {
                    criarDialogCRUD(IFlagment.DELETAR);
                } else {
                    criarDialogCRUD(IFlagment.EDITAR);
                }

                alert.dismiss();
            }

        };

        imgButtonNewCrud = dialogView.findViewById(R.id.imgButtonNewCrud);
        imgButtonDeleteCrud =  dialogView.findViewById(R.id.imgButtonDeleteCrud);
        imgButtonEditCrud = dialogView.findViewById(R.id.imgButtonEditCrud);

        imgButtonNewCrud.setOnClickListener(criarDialog);
        imgButtonDeleteCrud.setOnClickListener(criarDialog);
        imgButtonEditCrud.setOnClickListener(criarDialog);

        dialogCrud.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                return;
            }
        });

        alert = dialogCrud.create();
        alert.show();
    }

    /**
     * Cria o dialog para realizar a operação solicitada
     *
     * @param operacao Operação clicada {NOVO, EDITAR, DELETAR}
     */
    private void criarDialogCRUD(final String operacao) {

        operacaoEscolhida = operacao;

        dialogBuilder = new AlertDialog.Builder(UserKey.this);

        inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_key, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("API CREDENTIALS" + " - " + operacao.toUpperCase());

        inpApiCredentialsKey = dialogView.findViewById(R.id.inpDialogApiCredentialsKey);
        inpApiCredentialsSecret = dialogView.findViewById(R.id.inpDialogApiCredentialsSecret);

        if (flagIsStateSaved) {
            verificarStateSalvo();
        }


        //Se não for CADASTRAR preenche o Dialog
        if (!operacao.equals(IFlagment.CADASTRAR)) try {
            inpApiCredentialsKey.setText(apiCredentials.getKey());
            inpApiCredentialsSecret.setText(apiCredentials.getSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                apiCredentials.setKey(inpApiCredentialsKey.getText().toString());
                apiCredentials.setSecret(inpApiCredentialsSecret.getText().toString());

                //Foi validado?
                String mensagem = validar(apiCredentials);
                if (!(mensagem.length() == 0)) {
                    Toast.makeText(UserKey.this, mensagem, Toast.LENGTH_SHORT).show();
                    return;
                }

                executar();

            }
        })


                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        return;

                    }
                });

        dialog = dialogBuilder.create();
        dialog.show();

    }

    private void verificarStateSalvo() {
        inpApiCredentialsKey.setText(apiCredentialsKeySaved);
        inpApiCredentialsSecret.setText(apiCredentialsSecretSaved);
    }

    /**
     * Valida a entidade recebida
     *
     * @return returno uma string vazia se foi validada
     * ou uma string com a mensagem do erro
     */
    private String validar(ApiCredentials apiCredentials) {

        String retorno = "";

        if (apiCredentials.getKey().length() < 1) {
            retorno = "O campo KEY esta vazio.";
        } else if (apiCredentials.getSecret().length() < 1) {
            retorno = "O campo SECRET esta vazio.";
        }

        return retorno;
    }

    /**
     * Execute o CRUD
     */
    private void executar() {

        foiPersistido = false;
        if (operacaoEscolhida.equals(IFlagment.DELETAR))
            apiCredentialsDAO.delete(apiCredentials);
        else if (operacaoEscolhida.equals(IFlagment.EDITAR))
            apiCredentialsDAO.update(apiCredentials);
        else
            apiCredentialsDAO.create(apiCredentials);

        atualizarListView();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (inpApiCredentialsKey != null && inpApiCredentialsKey != null) {
            outState.putString("apiCredentialsKeySaved", inpApiCredentialsKey.getText().toString());
            outState.putString("apiCredentialsSecretSaved", inpApiCredentialsSecret.getText().toString());
        }

    }


    public void atualizarListView() {

        listaApiCredentials = apiCredentialsDAO.all();
        if (adapterApiCredentials == null) {
            adapterApiCredentials = new AdapterKeys(UserKey.this, listaApiCredentials);
            lstApiCredentials.setAdapter(adapterApiCredentials);
        } else {
            adapterApiCredentials.clear();
            adapterApiCredentials.notifyDataSetChanged();
            adapterApiCredentials.addAll(listaApiCredentials);
        }

    }


}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterConfiguracoes;
import br.com.bittrexanalizer.database.dao.ConfiguracaoDAO;
import br.com.bittrexanalizer.domain.Configuracao;
import br.com.bittrexanalizer.utils.ConfiguracaoComparator;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 16/01/2018.
 */

public class UserConfiguracao extends Activity implements IFlagment {


    private Configuracao configuracao;
    private ListView lstConfiguracoes;
    private AdapterConfiguracoes adapterConfiguracoes;
    private LinkedList<Configuracao> configuracoes;

    private ImageButton imgButtonNewCrud;
    private ImageButton imgButtonDeleteCrud;
    private ImageButton imgButtonEditCrud;

    private boolean foiPersistido = false;
    private String operacaoEscolhida = "";

    private LayoutInflater inflater;
    private AlertDialog alert;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;

    private ConfiguracaoDAO configuracaoDAO;

    private Handler handler = new Handler();
    private EditText inpConfiguracaoValor;
    private EditText inpConfiguracaoPropriedade;

    //variables bundle
    private boolean flagIsStateSaved = false;
    private String configuracaoValorSaved;
    private String configuracaoPropriedadeSaved;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_configuration);

        if (savedInstanceState != null) {
            flagIsStateSaved = true;
            configuracaoValorSaved = savedInstanceState.getString("configuracaoValorSaved");
            configuracaoPropriedadeSaved = savedInstanceState.getString("configuracaoPropriedadeSaved");
        }

        lstConfiguracoes = findViewById(R.id.lstConfiguracoes);
        configuracao = new Configuracao();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabConfiguracao);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configuracao = new Configuracao();
                criarDialogCRUD(IFlagment.CADASTRAR);
            }
        });


        configuracaoDAO = new ConfiguracaoDAO(UserConfiguracao.this);

        atualizarListView();

        lstConfiguracoes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                configuracao = new Configuracao();
                configuracao = (Configuracao) lstConfiguracoes.getItemAtPosition(i);

                showDialog();

                return true;
            }
        });

        Toast.makeText(this, "CONFIGURAÇÕES NECESSÁRIA PARA O SISTEMA FUNCIONAR CORRETAMENTE", Toast.LENGTH_SHORT).show();

    }

    /**
     * Exibe o dialog para escolher a opçao de CRUD
     * Variavel que indica se foi clicado de do fat(TRUE) ou do ListView(false)
     * Se foi clicado do Flag nao ativa a opçao de exclusao e delete
     */
    private void showDialog() {

        final AlertDialog.Builder dialogCrud = new AlertDialog.Builder(UserConfiguracao.this);

        inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_crud, null);
        dialogCrud.setView(dialogView);
        dialogCrud.setTitle(IFlagment.MENSAGEM_DIALOG);

        final View.OnClickListener criarDialog = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imgButtonNewCrud.getId() == view.getId()) {
                    configuracao = new Configuracao();
                    criarDialogCRUD(IFlagment.CADASTRAR);
                } else if (imgButtonDeleteCrud.getId() == view.getId()) {
                    criarDialogCRUD(IFlagment.DELETAR);
                } else {
                    criarDialogCRUD(IFlagment.EDITAR);
                }

                alert.dismiss();
            }

        };

        imgButtonNewCrud = (ImageButton) dialogView.findViewById(R.id.imgButtonNewCrud);
        imgButtonDeleteCrud = (ImageButton) dialogView.findViewById(R.id.imgButtonDeleteCrud);
        imgButtonEditCrud = (ImageButton) dialogView.findViewById(R.id.imgButtonEditCrud);

        imgButtonNewCrud.setOnClickListener(criarDialog);
        imgButtonDeleteCrud.setOnClickListener(criarDialog);
        imgButtonEditCrud.setOnClickListener(criarDialog);

        dialogCrud.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                return;
            }
        });

        alert = dialogCrud.create();
        alert.show();
    }

    /**
     * Cria o dialog para realizar a operação solicitada
     *
     * @param operacao Operação clicada {NOVO, EDITAR, DELETAR}
     */
    private void criarDialogCRUD(final String operacao) {

        operacaoEscolhida = operacao;

        dialogBuilder = new AlertDialog.Builder(UserConfiguracao.this);

        inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_configuracao, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("CONFIGURAÇÃO" + " - " + operacao.toUpperCase());

        inpConfiguracaoPropriedade = (EditText) dialogView.findViewById(R.id.inpDialogConfiguracaoPropriedade);
        inpConfiguracaoValor = (EditText) dialogView.findViewById(R.id.inpDialogConfiguracaoValor);

        if (flagIsStateSaved) {
            verificarStateSalvo();
        }


        //Se não for CADASTRAR preenche o Dialog
        if (!operacao.equals(IFlagment.CADASTRAR)) {
            inpConfiguracaoPropriedade.setText(configuracao.getPropriedade());
            inpConfiguracaoValor.setText(configuracao.getValor());
        }

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                configuracao.setPropriedade(inpConfiguracaoPropriedade.getText().toString().toUpperCase());
                configuracao.setValor(inpConfiguracaoValor.getText().toString());

                //Foi validado?
                String mensagem = validar(configuracao);
                if (!(mensagem.length() == 0)) {
                    Toast.makeText(UserConfiguracao.this, mensagem, Toast.LENGTH_SHORT).show();
                    return;
                }

                executar();

            }
        })


                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        return;

                    }
                });

        dialog = dialogBuilder.create();
        dialog.show();

    }

    private void verificarStateSalvo() {
        inpConfiguracaoPropriedade.setText(configuracaoPropriedadeSaved);
        inpConfiguracaoValor.setText(configuracaoValorSaved);
    }

    /**
     * Valida a entidade recebida
     *
     * @return returno uma string vazia se foi validada
     * ou uma string com a mensagem do erro
     */
    private String validar(Configuracao configuracao) {

        String retorno = "";

        if (configuracao.getPropriedade().length() < 1) {
            return retorno = "O campo Propriedade esta vazio.";
        } else if (configuracao.getValor().length() < 1) {
            return retorno = "O campo Valor esta vazio.";
        }

        return retorno;
    }

    /**
     * Execute o CRUD
     */
    private void executar() {

        foiPersistido = false;
        if (operacaoEscolhida.equals(IFlagment.DELETAR))
            configuracaoDAO.delete(configuracao);
        else if (operacaoEscolhida.equals(IFlagment.EDITAR))
            configuracaoDAO.update(configuracao);
        else
            configuracaoDAO.create(configuracao);

        atualizarListView();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (inpConfiguracaoValor != null && inpConfiguracaoPropriedade != null) {
            outState.putString("configuracaoPropriedadeSaved", inpConfiguracaoPropriedade.getText().toString());
            outState.putString("configuracaoValorSaved", inpConfiguracaoValor.getText().toString());
        }

    }


    public void atualizarListView() {

        configuracoes = configuracaoDAO.all();

        configuracoes = ConfiguracaoComparator.ordenar(ConfiguracaoComparator.PROP_ASC, configuracoes);

        if (adapterConfiguracoes == null) {
            adapterConfiguracoes = new AdapterConfiguracoes(UserConfiguracao.this, configuracoes);
            lstConfiguracoes.setAdapter(adapterConfiguracoes);
        } else {
            adapterConfiguracoes.clear();
            adapterConfiguracoes.notifyDataSetChanged();
            adapterConfiguracoes.addAll(configuracoes);
        }

    }


    public void atualizarSessionUtil(){

            LinkedList<Configuracao> configuracoes = new LinkedList<>();
            configuracoes = new ConfiguracaoDAO(UserConfiguracao.this).all();

            Map<String, String> mapConfiguracao = new HashMap<String, String>();

            if(configuracoes==null){
                SessionUtil.getInstance().setMapConfiguracao(null);
                return;
            }

            for(Configuracao c : configuracoes){
                mapConfiguracao.put(c.getPropriedade(), c.getValor());
            }

            SessionUtil.getInstance().setMapConfiguracao(mapConfiguracao);



    }


}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Order;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class UserOrder extends Activity implements IFlagment {

    private LinkedList<Order> orders;
    private Order order;
    private String UUID;
    private SwipeRefreshLayout swipeRefreshMain;
    private Handler handler = new Handler();

    private TextView txtOrderUuid;
    private TextView txtExchange;
    private TextView txtTimeStamp;
    private TextView txtOrderType;
    private TextView txtLimit;
    private TextView txtQuantity;
    private TextView txtQuantityRemaining;
    private TextView txtComission;
    private TextView txtPrice;
    private TextView txtPricePerUnit;
    private TextView txtIsConditional;
    private TextView txtCondition;
    private TextView txtConditionTarget;
    private TextView txtImmediateOrCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_order);

        txtExchange = findViewById(R.id.txtExchange);
        txtOrderType = findViewById(R.id.txtOrderType);
        txtOrderUuid= findViewById(R.id.txtOrderUuid);
        txtTimeStamp = findViewById(R.id.txtTimeStamp);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtQuantityRemaining = findViewById(R.id.txtQuantityRemaining);
        txtLimit = findViewById(R.id.txtLimit);
        txtComission = findViewById(R.id.txtComission);
        txtIsConditional = findViewById(R.id.txtIsConditional);
        txtCondition = findViewById(R.id.txtCondition);
        txtConditionTarget = findViewById(R.id.txtConditionTarget);
        txtImmediateOrCancel = findViewById(R.id.txtImmediateOrCancel);
        txtPrice = findViewById(R.id.txtPrice);
        txtPricePerUnit = findViewById(R.id.txtPricePerUnit);

        order = new Order();
        //Pega o numero do UUID recebido na Intent
        order = (Order) getIntent().getExtras().get("ORDER");

        if(order==null){
            Toast.makeText(this, "Erro ao Executar o Comando", Toast.LENGTH_SHORT).show();
        }

        preencherDados(order);

    }


    /**
     * Preenche dos dados da Order na View
     * @param order
     */
    private void preencherDados(Order order) {


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        txtExchange.setText(order.getExchange());
        txtOrderType.setText(order.getOrderType());
        txtTimeStamp.setText(sdf.format(order.getTimeStamp().getTime()));
        txtOrderUuid.setText(order.getOrderUuid());
        txtLimit.setText(order.getLimit().toString());
        txtQuantity.setText(order.getQuantity().toString());
        txtQuantityRemaining.setText(order.getQuantityRemaining().toString());
        txtComission.setText(order.getComission().toString());
        txtPrice.setText(order.getPrice().toString());
        txtPricePerUnit.setText(order.getPricePerUnit().toString());
        txtIsConditional.setText(order.getIsConditional().toString());
        txtCondition.setText(order.getCondition().toString());
        txtConditionTarget.setText(order.getConditionTarget().toString());
        txtImmediateOrCancel.setText(order.getImmediateOrCancel().toString());

    }

    @Override
    public void atualizarListView() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.database.dao.TickerDAO;
import br.com.bittrexanalizer.domain.Ticker;

/**
 * Created by PauLinHo on 08/02/2018.
 */

public class UserTicker extends Activity {

    private Ticker ticker;
    private TextView txtSigla, txtAsk,
            txtBid, txtStopLost, txtStopLimit, txtExchange,txtValorDeCompra;
    private EditText inpIsBought;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_ticker);

        ticker = (Ticker) getIntent().getSerializableExtra("TICKER");

        txtSigla = findViewById(R.id.txtSigla);
        inpIsBought = findViewById(R.id.inpIsBought);
        txtAsk = findViewById(R.id.txtAsk);
        txtBid = findViewById(R.id.txtBid);
        txtStopLost = findViewById(R.id.txtStopLoss);
        txtStopLimit = findViewById(R.id.txtStopLimit);
        txtValorDeCompra = findViewById(R.id.txtValorDeCompra);
        txtExchange = findViewById(R.id.txtNomeExchange);

        txtSigla.setText(ticker.getSigla());
        inpIsBought.setText(ticker.getBought().toString().toUpperCase());
        txtExchange.setText(ticker.getNomeExchange().toUpperCase());
        txtAsk.setText(ticker.getAsk().toString());
        txtBid.setText(ticker.getBid().toString());
        txtStopLimit.setText(ticker.getAvisoStopGain().toString());
        txtStopLost.setText(ticker.getAvisoStopLoss().toString());
        BigDecimal valorDeCompra = ticker.getValorDeCompra();

        if(valorDeCompra.compareTo(BigDecimal.ZERO)==0){
            txtValorDeCompra.setText("-");
        }else{
            txtValorDeCompra.setText(valorDeCompra.toString());
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        //Se teve alteração será salvo
        String valorIsBought = inpIsBought.getText().toString();
        String mensagem = "";
        if (!valorIsBought.toLowerCase().equals(ticker.getBought().toString().toLowerCase())) {
            TickerDAO tickerDAO = new TickerDAO(UserTicker.this);
            if (valorIsBought.toLowerCase().equals("true") ||
                    valorIsBought.toLowerCase().equals("false")) {

                ticker.setBought(new Boolean(valorIsBought));

                tickerDAO.update(ticker);
                mensagem += "Atualizado no Banco de Dados.";
            } else {
                mensagem += "Valor deve ser TRUE ou FALSE";
            }
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        }

    }
}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterOpenOrders;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.strategy.CancelOrderStrategy;
import br.com.bittrexanalizer.strategy.IStrategy;
import br.com.bittrexanalizer.strategy.OpenOrderStrategy;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class UserOpenOrders extends Activity implements IFlagment {

    private ListView lstOrders;
    private LinkedList<Order> orders;
    private AdapterOpenOrders adapterOrders;
    private SwipeRefreshLayout swipeRefreshMain;
    private Handler handler = new Handler();
    private AlertDialog alertDialog;
    private EmailUtil emailUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);

        lstOrders = findViewById(R.id.lstOpenOrders);
        swipeRefreshMain = findViewById(R.id.swipeRefleshMain);

        swipeRefreshMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrders();
                swipeRefreshMain.setRefreshing(false);
            }
        });

        swipeRefreshMain.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        );

        getOrders();

        lstOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = (Order) lstOrders.getItemAtPosition(position);

                if (order != null) {
                    showDialogCancelOrder(order);
                } else {
                    Toast.makeText(UserOpenOrders.this, "Erro ao Abrir a Order", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void showDialogCancelOrder(final Order order) {

        AlertDialog.Builder alert = new AlertDialog.Builder(UserOpenOrders.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_order, null);
        alert.setView(dialogView);

        final TextView txtCurrency =  dialogView.findViewById(R.id.txtCurrency);
        final TextView txtQuantity =  dialogView.findViewById(R.id.txtQuantity);
        final TextView txtOpened =  dialogView.findViewById(R.id.txtOpened);
        final TextView txtCondition =  dialogView.findViewById(R.id.txtCondition);
        final TextView txtOrderType =  dialogView.findViewById(R.id.txtOrderType);


        txtCurrency.setText(order.getExchange());
        txtQuantity.setText(order.getQuantity().toString());
        txtOpened.setText(IStrategy.SDF_DDMMYYYY_HHMMSS.format(order.getOpened().getTime()));
        txtCondition.setText(order.getCondition());
        txtOrderType.setText(order.getOrderType());

        alert.setPositiveButton("CANCELAR ORDER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final boolean hasCanceled = CancelOrderStrategy.execute(order);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                    Toast.makeText(UserOpenOrders.this,
                                            "Operação executada", Toast.LENGTH_SHORT).show();

                                    emailUtil = new EmailUtil();
                                    emailUtil.enviarEmail(UserOpenOrders.this, ConstantesUtil.CANCEl_ORDER,
                                            "Order: UUId"+order.getOrderUuid()+
                                    " - Exchange: "+order.getExchange()+ " - VALOR: "+ order.getLimit() +
                                    " \nCancelada com sucesso", "CANCEL_ORDER");

                            }
                        });

                        recreate();

                    }
                }
        )


                .setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        return;
                    }
                });

        alertDialog = alert.create();
        alertDialog.setIcon(R.drawable.ic_delete_black_24dp);
        alertDialog.show();
    }

    private void getOrders() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                orders = new LinkedList<>();

                boolean foiLocalizado = OpenOrderStrategy.execute();

                if(!foiLocalizado && SessionUtil.getInstance().getMapOpenOrders()==null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserOpenOrders.this, "Ocorreu um erro", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    for (Order order : SessionUtil.getInstance().getMapOpenOrders().values()) {
                        orders.add(order);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (orders.size() == 0) {
                            Toast.makeText(UserOpenOrders.this, "Sem dados para Exibição", Toast.LENGTH_LONG).show();
                        } else {
                            atualizarListView();
                        }

                    }
                });
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void atualizarListView() {

        if (adapterOrders == null) {
            adapterOrders = new AdapterOpenOrders(UserOpenOrders.this, orders);
            lstOrders.setAdapter(adapterOrders);
        } else {
            adapterOrders.clear();
            adapterOrders.notifyDataSetChanged();
            adapterOrders.addAll(orders);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


package br.com.bittrexanalizer.telas;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterBalances;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.strategy.BalanceStrategy;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class UserBalances extends Activity implements IFlagment {

    private ListView lstBalances;
    private LinkedList<Balance> balances;
    private AdapterBalances adapterBalances;
    private SwipeRefreshLayout swipeRefreshMain;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        lstBalances = findViewById(R.id.lstBalances);
        swipeRefreshMain = findViewById(R.id.swipeRefleshMain);

        swipeRefreshMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBalances();
                swipeRefreshMain.setRefreshing(false);
            }
        });

        swipeRefreshMain.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        );

        getBalances();

    }

    private void getBalances() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                balances = new LinkedList<>();

                boolean foiLocalizado = BalanceStrategy.execute();

                if(!foiLocalizado){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserBalances.this, "Ocorreu um erro", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    for (Balance balance : SessionUtil.getInstance().getMapBalances().values()) {
                        balances.add(balance);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (balances.size() == 0) {
                            Toast.makeText(UserBalances.this, "Sem dados para Exibição", Toast.LENGTH_LONG).show();
                        } else {
                            atualizarListView();
                        }

                    }
                });

            }
        });
        t.start();


    }

    @Override
    public void atualizarListView() {

        if (adapterBalances == null) {
            adapterBalances = new AdapterBalances(UserBalances.this, balances);
            lstBalances.setAdapter(adapterBalances);
        } else {
            adapterBalances.clear();
            adapterBalances.notifyDataSetChanged();
            adapterBalances.addAll(balances);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}


package br.com.bittrexanalizer.telas;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.adapters.AdapterExchanges;
import br.com.bittrexanalizer.api.ApiCredentials;
import br.com.bittrexanalizer.database.bd.BittrexBD;
import br.com.bittrexanalizer.database.dao.ApiCredentialsDAO;
import br.com.bittrexanalizer.database.dao.ConfiguracaoDAO;
import br.com.bittrexanalizer.database.dao.TickerDAO;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.domain.Configuracao;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.receivers.ServiceAvisoTaxa;
import br.com.bittrexanalizer.receivers.ServiceCompra;
import br.com.bittrexanalizer.receivers.ServiceVenda;
import br.com.bittrexanalizer.strategy.AlarmAnalizerCompraStrategy;
import br.com.bittrexanalizer.strategy.BalanceStrategy;
import br.com.bittrexanalizer.strategy.BuyOrderStrategy;
import br.com.bittrexanalizer.strategy.CancelOrderStrategy;
import br.com.bittrexanalizer.strategy.OpenOrderStrategy;
import br.com.bittrexanalizer.strategy.SellOrderStrategy;
import br.com.bittrexanalizer.strategy.StopOrderStrategy;
import br.com.bittrexanalizer.utils.CalculoUtil;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;
import br.com.bittrexanalizer.utils.VerificaConexaoStrategy;
import br.com.bittrexanalizer.utils.WebServiceUtil;
import br.com.bittrexanalizer.webserver.HttpClient;

import static br.com.bittrexanalizer.utils.TickerComparator.ordenar;

public class MainActivityDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    public static MainActivityDrawer instance;

    private ListView lstTicker;
    private AdapterExchanges adapterExchanges;
    private View viewSuperior;
    private SwipeRefreshLayout swipeRefreshMain;
    private LinearLayout lnlTitulo;
    private LinearLayout lnlColunas;

    private EmailUtil emailUtil = new EmailUtil();

    private EditText inpValorAvisoBuyAbaixo, inpValorAvisoBuyAcima,
            inpValorAvisoSellAbaixo, inpValorAvisoSellAcima, inpValorDeCompra;
    private ToggleButton tbBuyAbaixo, tbBuyAcima, tbSellAbaixo, tbSellAcima;

    private Ticker ticker;
    private volatile LinkedList<Ticker> tickers;
    private TickerDAO tickerDAO;
    private BittrexBD bittrexBD;

    private Menu menu;
    private Map<String, String> mapSiglaNomeExchange;

    private TextView txtCalcular;
    private TextView txtDeletar;
    private TextView txtCriarNotificacao;
    private TextView txtCriarCompra;
    private TextView txtVender;
    private TextView txtDetalhar;
    private TextView txtCriarStop;
    private boolean hasInternet = true;

    private int qtde;

    private final Timer myTimer = new Timer();

    private ProgressDialog dialog;
    private AlertDialog alert;
    private AlertDialog alertDialog;

    private EditText inpQuantidadeDeBTCParaComprar;
    private EditText inpValorStop;

    private final Double TAXA_BUY = 0.0025;
    private final Double TAXA_SELL = 0.0025;

    private final String LOG = "BITTREX";

    private int contadorNomeClassificacao = 0;
    private int contadorLastClassificacao = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        menu = navigationView.getMenu();

        lstTicker = (ListView) findViewById(R.id.lstTickers);
        viewSuperior = (View) findViewById(R.id.viewSuperior);
        lnlTitulo = (LinearLayout) findViewById(R.id.lnlTitulo);
        lnlColunas = (LinearLayout) findViewById(R.id.lnlColunas);
        swipeRefreshMain = (SwipeRefreshLayout) findViewById(R.id.swipeRefleshMain);
        swipeRefreshMain.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTicker();
                swipeRefreshMain.setRefreshing(false);
            }
        });
        swipeRefreshMain.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
        );

        int i = getResources().getConfiguration().orientation;
        if (i == 2) {
            viewSuperior.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.6f));
            lnlTitulo.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.4f));
            lnlColunas.setLayoutParams(new AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.9f));
        }

        mapSiglaNomeExchange = new HashMap<>();

        bittrexBD = new BittrexBD(this);
        //bittrexBD.onCreate(ConnectionFactory.getConnection(this));
        tickerDAO = new TickerDAO(this);

        dialog = new ProgressDialog(MainActivityDrawer.this);
        dialog.setMessage("Processando...");
        dialog.setTitle("BittrexAnalizer");
        dialog.show();

        hasInternet = VerificaConexaoStrategy.verificarConexao(MainActivityDrawer.this);

        if (!hasInternet) {
            Toast.makeText(this, "Necessário conexão com a Internet", Toast.LENGTH_LONG).show();
            finish();
        }

        getConfiguracoes();
        verificarVariaveisDeSistema();
        startServiceTaxa();
        getApiCredentials();
        getTicker();

        verificarMenuService();

        lstTicker.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                ticker = new Ticker();
                ticker = (Ticker) lstTicker.getItemAtPosition(i);

                showDialogOption();

                return true;
            }
        });

        Thread t = new Thread(this);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        myTimer.scheduleAtFixedRate(new MyTask(), 0, (long) (300000));

        if (qtde > 0)
            Toast.makeText(getApplicationContext(), "Clique em cima da Moeda para ver as Opções", Toast.LENGTH_SHORT).show();

        instance = this;

    }

    /**
     * Salva valores no SharedPreferences
     *
     * @param key
     * @param value
     * @return
     */
    private boolean salvarPreferences(String key, String value) {
        SharedPreferences.Editor editor = MainActivityDrawer.this.getSharedPreferences("SERVICES", Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        return editor.commit();
    }

    private String getPreferences(String key) {
        SharedPreferences preferences = getSharedPreferences("SERVICES", MODE_PRIVATE);
        return preferences.getString(key, "false");
    }

    private void verificarMenuService() {

        boolean isStartCompraService = new Boolean(getPreferences("SERVICE_COMPRA"));
        boolean isStartVendaService = new Boolean(getPreferences("SERVICE_VENDA"));

        //verifica se está stopado o servico de compra
        if (isStartCompraService) {
            menu.getItem(2).getSubMenu().getItem().getSubMenu().getItem(0).setVisible(false);
            menu.getItem(2).getSubMenu().getItem().getSubMenu().getItem(1).setVisible(true);
        } else {
            menu.getItem(2).getSubMenu().getItem().getSubMenu().getItem(0).setVisible(true);
            menu.getItem(2).getSubMenu().getItem().getSubMenu().getItem(1).setVisible(false);
        }

        //verifica se está stopado o servico de venda
        if (isStartVendaService) {
            menu.getItem(3).getSubMenu().getItem().getSubMenu().getItem(0).setVisible(false);
            menu.getItem(3).getSubMenu().getItem().getSubMenu().getItem(1).setVisible(true);
        } else {
            menu.getItem(3).getSubMenu().getItem().getSubMenu().getItem(0).setVisible(true);
            menu.getItem(3).getSubMenu().getItem().getSubMenu().getItem(1).setVisible(false);
        }

    }

    /**
     * Start o Service de Aviso de Taxa, que possibilita a geração de Notificações
     */
    private void startServiceTaxa() {
        Intent i = new Intent(MainActivityDrawer.this, ServiceAvisoTaxa.class);
        startService(i);
    }

    private void stopServiceTaxa() {
        Intent i = new Intent(MainActivityDrawer.this, ServiceAvisoTaxa.class);
        stopService(i);
    }

    private void startServiceCompra() {
        Intent i = new Intent(MainActivityDrawer.this, ServiceCompra.class);
        startService(i);

        salvarPreferences("SERVICE_COMPRA", "true");

    }

    private void stopServiceCompra() {
        Intent i = new Intent(MainActivityDrawer.this, ServiceCompra.class);
        stopService(i);

        salvarPreferences("SERVICE_COMPRA", "false");

    }

    private void startServiceVenda() {
        Intent i1 = new Intent(MainActivityDrawer.this, ServiceVenda.class);
        startService(i1);

        salvarPreferences("SERVICE_VENDA", "true");
    }

    private void stopServiceVenda() {
        Intent i1 = new Intent(MainActivityDrawer.this, ServiceVenda.class);
        stopService(i1);

        salvarPreferences("SERVICE_VENDA", "false");
    }

    /**
     * SE NÃO EXISTIR AS PRINCIPAIS CONFIGURAÇÕES DO SISTEMA SERÁ NECESSARIO CRIAR
     */
    private void verificarVariaveisDeSistema() {


        //Configurações de notificações
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.EMAIL)) {
            createVariavel(ConstantesUtil.EMAIL, "paulinho.legionario07@gmail.com");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ANALISE_LIGADA)) {
            createVariavel(ConstantesUtil.ANALISE_LIGADA, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ROBOT_LIGADO)) {
            createVariavel(ConstantesUtil.ROBOT_LIGADO, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.VALOR_COMPRA_ROBOT)) {
            createVariavel(ConstantesUtil.VALOR_COMPRA_ROBOT, "0.00020000");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ROBOT_VEZES)) {
            createVariavel(ConstantesUtil.ROBOT_VEZES, "3");
        }


        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ENVIAR_EMAIL)) {
            createVariavel(ConstantesUtil.ENVIAR_EMAIL, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.TEMPO_ANALISE_MIN)) {
            createVariavel(ConstantesUtil.TEMPO_ANALISE_MIN, "120");

            SessionUtil.getInstance().setUltimoTempoDeNotificacaoSalvo(Long.valueOf("120"));
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.QTDE_TICKERS_PESQUISA)) {
            createVariavel(ConstantesUtil.QTDE_TICKERS_PESQUISA, "90");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.TEMPO_ESPERA_THREAD)) {
            createVariavel(ConstantesUtil.TEMPO_ESPERA_THREAD, "30000");

        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ENVIAR_NOTIFICACAO)) {
            createVariavel(ConstantesUtil.ENVIAR_NOTIFICACAO, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ROBOT_TEMPO_ENVIO_EMAIL)) {
            createVariavel(ConstantesUtil.ROBOT_TEMPO_ENVIO_EMAIL, "7200000");
        }

        /**
         * Configurações de ANALISES
         */
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.LONG_EMA)) {
            createVariavel(ConstantesUtil.LONG_EMA, "26");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.SHORT_EMA)) {
            createVariavel(ConstantesUtil.SHORT_EMA, "12");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.SIGNAL)) {
            createVariavel(ConstantesUtil.SIGNAL, "9");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.IFR_DIAS)) {
            createVariavel(ConstantesUtil.IFR_DIAS, "14");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.IFR_MIN)) {
            createVariavel(ConstantesUtil.IFR_MIN, "30");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.IFR_MAX)) {
            createVariavel(ConstantesUtil.IFR_MAX, "70");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.MACD)) {
            createVariavel(ConstantesUtil.MACD, "true");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.IFR)) {
            createVariavel(ConstantesUtil.IFR, "true");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OSCILADOR_ESTOCASTICO)) {
            createVariavel(ConstantesUtil.OSCILADOR_ESTOCASTICO, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OBV)) {
            createVariavel(ConstantesUtil.OBV, "true");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OBV_QTDE_FECHAMENTOS)) {
            createVariavel(ConstantesUtil.OBV_QTDE_FECHAMENTOS, "3");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OE_TEMPO_PERIODO_K)) {
            createVariavel(ConstantesUtil.OE_TEMPO_PERIODO_K, "14");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OE_TEMPO_PERIODO_D)) {
            createVariavel(ConstantesUtil.OE_TEMPO_PERIODO_D, "3");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OE_TAXA_MIN)) {
            createVariavel(ConstantesUtil.OE_TAXA_MIN, "20");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.OE_TAXA_MAX)) {
            createVariavel(ConstantesUtil.OE_TAXA_MAX, "80");
        }

        /**
         * Configurações de TRADER
         */
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.STOP_LOSS)) {
            createVariavel(ConstantesUtil.STOP_LOSS, "97");
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.STOP_GAIN)) {
            createVariavel(ConstantesUtil.STOP_GAIN, "101");
        }

        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.PERIODICIDADE)) {
            createVariavel(ConstantesUtil.PERIODICIDADE, WebServiceUtil.getTickintervalOneMin());
        }
        if (!SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ALL_TICKERS)) {
            createVariavel(ConstantesUtil.ALL_TICKERS, "false");
        }


        getConfiguracoes();


    }

    private void createVariavel(String propriedade, String valor) {

        ConfiguracaoDAO configuracaoDAO = null;

        if (configuracaoDAO == null) {
            configuracaoDAO = new ConfiguracaoDAO(MainActivityDrawer.this);
        }

        Configuracao c = new Configuracao();
        c.setPropriedade(propriedade);
        c.setValor(valor);
        configuracaoDAO.create(c);
    }

    private void getConfiguracoes() {

        LinkedList<Configuracao> configuracoes = new LinkedList<>();
        configuracoes = new ConfiguracaoDAO(MainActivityDrawer.this).all();

        Map<String, String> mapConfiguracao = new HashMap<String, String>();

        if (configuracoes == null) {
            SessionUtil.getInstance().setMapConfiguracao(null);
            return;
        }

        for (Configuracao c : configuracoes) {
            mapConfiguracao.put(c.getPropriedade(), c.getValor());
        }

        SessionUtil.getInstance().setMapConfiguracao(mapConfiguracao);


    }

    private void getApiCredentials() {

        ApiCredentials apiKey = new ApiCredentials();
        apiKey.setId(1l);
        apiKey = new ApiCredentialsDAO(MainActivityDrawer.this).find(apiKey);

        SessionUtil.getInstance().setApiCredentials(apiKey);

    }

    private synchronized void getTickers() {

        tickers = new LinkedList<>();
        tickers = tickerDAO.findAllTickers();

        if (tickers.size() == 0) {
            return;
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Ticker t : tickers) {
            ticker = t;
            ticker.setUrlApi(WebServiceUtil.getUrl() + ticker.getSigla().toLowerCase());
            executorService.execute(t);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Atualizando a Lista do SessionUtil
        SessionUtil.getInstance().setTickers(new LinkedList<Ticker>());
        SessionUtil.getInstance().setTickers(tickers);

    }

    private void getTicker() {

        try {
            getTickers();

            qtde = tickers.size();
            Collections.sort(tickers);

            atualizarListView(tickers);

        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
        } finally {
            dialog.dismiss();
        }

    }


    /**
     * Cria o Dialog com as opçoes Inserir, Editar e Excluir
     */
    private void showDialogOption() {

        final AlertDialog.Builder dialogCrud = new AlertDialog.Builder(MainActivityDrawer.this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_option_list_view, null);
        dialogCrud.setView(dialogView);
        dialogCrud.setTitle("Escolha a Opção");


        View.OnClickListener criarDialog = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtCalcular.getId() == view.getId()) {
                    showCalcularDialog(ticker);
                } else if (txtDeletar.getId() == view.getId()) {
                    showDialogDeletarMoeda();
                } else if (txtCriarNotificacao.getId() == view.getId()) {
                    showDialogCriarNotificacao();
                } else if (txtDetalhar.getId() == view.getId()) {
                    Intent i = new Intent(MainActivityDrawer.this, UserTicker.class);
                    i.putExtra("TICKER", ticker);
                    startActivity(i);
                } else if (txtCriarCompra.getId() == view.getId()) {
                    if (inpQuantidadeDeBTCParaComprar.getText().toString().length() < 0) {
                        Toast.makeText(MainActivityDrawer.this, "Valor invalido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    criarCompra();
                } else if (txtVender.getId() == view.getId()) {
                    //Abre um dialogo para confirmar a venda
                    AlertDialog.Builder alertVenda = new AlertDialog.Builder(MainActivityDrawer.this);
                    alertVenda.setTitle("CONFIRMAR VENDA?");
                    alertVenda.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            criarVenda();
                        }
                    });
                    alertVenda.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            return;
                        }
                    });

                    AlertDialog alert = alertVenda.create();
                    alert.show();

                }


                alert.cancel();

            }


        };

        txtCalcular = dialogView.findViewById(R.id.txtCalcular);
        txtDeletar = dialogView.findViewById(R.id.txtDeletar);
        txtCriarNotificacao = dialogView.findViewById(R.id.txtCriarNotificacao);
        txtCriarCompra = dialogView.findViewById(R.id.txtCriarCompra);
        txtCriarStop = dialogView.findViewById(R.id.txtCriarStopLoss);
        txtVender = dialogView.findViewById(R.id.txtVender);
        txtDetalhar = dialogView.findViewById(R.id.txtDetalhar);

        inpQuantidadeDeBTCParaComprar = dialogView.findViewById(R.id.inpQuantidadeDeBTCParaComprar);
        inpValorStop = dialogView.findViewById(R.id.inpValorStop);

        txtCalcular.setOnClickListener(criarDialog);
        txtDeletar.setOnClickListener(criarDialog);
        txtCriarNotificacao.setOnClickListener(criarDialog);
        txtCriarStop.setOnClickListener(criarDialog);
        txtCriarCompra.setOnClickListener(criarDialog);
        txtVender.setOnClickListener(criarDialog);
        txtDetalhar.setOnClickListener(criarDialog);


        dialogCrud.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                return;
            }
        });

        alert = dialogCrud.create();
        alert.show();
    }

    /**
     * Cria um stop de venda, deve ser menor que o valor do objeto
     */
    private synchronized void criarStop() {

        Order order = new Order();

        //Existe um stop ja?
        if (SessionUtil.getInstance().getMapOpenOrders().containsKey(ticker.getSigla())) {
            order = SessionUtil.getInstance().getMapOpenOrders().get(ticker.getSigla());

            if (order.getOrderUuid() != null) {
                CancelOrderStrategy.execute(order);
            }

        } else {
            //Atualiza o map de Balances
            BalanceStrategy.execute();

            //Não existe stop, então será pega a quantidade total da moeda disponível
            Balance b = SessionUtil.getInstance().getMapBalances().get(ticker.getSigla());
            order.setQuantity(b.getBalance());
            order.setSigla(ticker.getSigla());
        }

        //pega o valor para o stop se for vazio será usado o configuração padrão
        if (inpValorStop.getText().toString().length() < 1) {
            BigDecimal porcentagem = new BigDecimal(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.STOP_LOSS));
            order.setRate(CalculoUtil.getPorcentagem(ticker.getBid(), porcentagem));
        } else {
            order.setRate(new BigDecimal(inpValorStop.getText().toString()));
        }

        //Executa a orderm de STOP
        boolean retorno = StopOrderStrategy.execute(order);

        //cria a mensagem do EMAIL
        final String mensagem = criarMensagemOperacao(ticker.getSigla(), order.getRate(), order.getQuantity());

        if (retorno) {
            emailUtil.enviarEmail(MainActivityDrawer.this, ConstantesUtil.COMPRA_REALIZADA,
                    mensagem, "STOP");

        } else {
            Toast.makeText(this, "Erro ao executar operação", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Realiza uma compra com o valor atual de compra da moeda
     */
    private synchronized void criarCompra() {

        try {
            Order order = new Order();
            order.setSigla(ticker.getSigla());

            BigDecimal valor = new BigDecimal(inpQuantidadeDeBTCParaComprar.getText().toString()).setScale(8);

            //Calcula a quantidade da moeda que será comprada
            order.setQuantity(CalculoUtil.getQuantidadeASerComprada(valor, ticker.getAsk()));

            //Pega o valor atual de compra da moeda
            order.setRate(ticker.getAsk());

            //executa a compra
            boolean retorno = BuyOrderStrategy.execute(order);

            final String mensagem = criarMensagemOperacao(ticker.getSigla(), order.getRate(), order.getQuantity());

            if (retorno) {

                emailUtil.enviarEmail(MainActivityDrawer.this, ConstantesUtil.STOP_CRIADO,
                        mensagem, "COMPRA");

            } else {
                Toast.makeText(this, "Erro ao executar operação", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            new EmailUtil().enviarEmail(MainActivityDrawer.this, "ERRO", e.getMessage());

        }

    }


    /**
     * Realiza uma venda com o valor atual do ticekr
     */
    private synchronized void criarVenda() {

        try {
            Order order = new Order();

            //Atualiza as orders
            OpenOrderStrategy.execute();

            //verifica se tem uma order aberta para essa moeda
            if (SessionUtil.getInstance().getMapOpenOrders().containsKey(ticker.getSigla())) {
                order = SessionUtil.getInstance().getMapOpenOrders().get(ticker.getSigla());

                //Se tiver é necessário cancelar a ordem de stop
                if (order.getOrderUuid() != null) {
                    boolean cancelou = CancelOrderStrategy.execute(order);
                }
            } else {
                //Atualiza o map de Balances
                BalanceStrategy.execute();

                //Verifica se existe saldo para essa moeda
                if (!SessionUtil.getInstance().getMapBalances().containsKey(ticker.getSigla())) {
                    Toast.makeText(MainActivityDrawer.this, "Não há Saldo Disponível para essa moeda", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //Se existir pega o valor
                    Balance b = SessionUtil.getInstance().getMapBalances().get(ticker.getSigla());
                    order.setQuantity(b.getBalance());
                    order.setSigla(ticker.getSigla());
                }
            }

            //Pega o valor atual de venda
            order.setRate(ticker.getBid());

            //Realiza a venda
            boolean retorno = SellOrderStrategy.execute(order);

            //cria a mensagem do email
            final String mensagem = criarMensagemOperacao(ticker.getSigla(), order.getRate(), order.getQuantity());

            if (retorno) {
                emailUtil.enviarEmail(MainActivityDrawer.this, ConstantesUtil.VENDA_REALIZADA,
                        mensagem, "VENDA");

            } else {
                Toast.makeText(this, "Erro ao executar operação", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            new EmailUtil().enviarEmail(MainActivityDrawer.this, "ERRO", e.getMessage());

        }

    }

    /**
     * Cria mensagem para enviar via email após uma operação de COMPRA, VENDA, STOP
     *
     * @param sigla      - Sigla da Moeda
     * @param valor      - Valor da Operação
     * @param quantidade - Quantidade da Moeda
     * @return
     */
    private String criarMensagemOperacao(String sigla, BigDecimal valor, BigDecimal quantidade) {

        StringBuilder mensagem = new StringBuilder();
        mensagem.append("EXCHANGE: ");
        mensagem.append(sigla);
        mensagem.append("\tVALOR: ");
        mensagem.append(valor);
        mensagem.append("\tQUANTIDADE: ");
        mensagem.append(quantidade);

        return mensagem.toString();
    }


    /**
     * Calcula os valores de uma Compra e uma Venda
     *
     * @param ticker - Recebe um Ticker com os dados
     */
    private void showCalcularDialog(final Ticker ticker) {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);
        AlertDialog dialog;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_calcular, null);
        alert.setView(dialogView);

        final EditText inpTotalEmBTCParaCompra = (EditText) dialogView.findViewById(R.id.inpTotalEmBTCParaCompra);
        final EditText inpValorCoinBuy = (EditText) dialogView.findViewById(R.id.inpValorCoinBuy);
        final EditText inpValorCoinSell = (EditText) dialogView.findViewById(R.id.inpValorCoinSell);
        final TextView txtTotalCoinAdquirido = (TextView) dialogView.findViewById(R.id.txtTotalCoinAdquirido);
        final TextView txtTotalBTCAdquirido = (TextView) dialogView.findViewById(R.id.txtTotalBTCAdquirido);
        final TextView txtCalcularROI = (TextView) dialogView.findViewById(R.id.txtCalcularROI);
        final ImageButton imgCalcular = (ImageButton) dialogView.findViewById(R.id.imgCalcular);

        txtCalcularROI.setText("Calcular ROI - " + ticker.getSigla());

        if(ticker.getValorDeCompra().compareTo(BigDecimal.ZERO)==1){
            inpValorCoinBuy.setText(ticker.getValorDeCompra().toString());
        }

        //Setando os hints
        inpValorCoinBuy.setHint(getHintTaxaCompra(ticker));
        inpValorCoinSell.setHint(getHintTaxaVenda(ticker));
        txtTotalCoinAdquirido.setHint(getHintTotalAdiquirido(ticker));

        imgCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (inpTotalEmBTCParaCompra.getText().length() < 1 || new BigDecimal(inpTotalEmBTCParaCompra.getText().toString()).compareTo(new BigDecimal("0.0")) != 1) {
                    Toast.makeText(getApplicationContext(), "Total para Compra inválido", Toast.LENGTH_LONG).show();
                } else if (inpValorCoinBuy.getText().length() < 1 || new BigDecimal(inpValorCoinBuy.getText().toString()).compareTo(new BigDecimal("0.0")) != 1) {
                    Toast.makeText(getApplicationContext(), "Taxa para Compra inválida", Toast.LENGTH_LONG).show();
                } else if (inpValorCoinSell.getText().length() < 1 || new BigDecimal(inpValorCoinSell.getText().toString()).compareTo(new BigDecimal("0.0")) != 1) {
                    Toast.makeText(getApplicationContext(), "Taxa para Venda inválida", Toast.LENGTH_LONG).show();
                } else {

                    //tudo foi validado
                    BigDecimal coinsOptidoPelaCompra = new BigDecimal(calcularCompra(new BigDecimal(inpTotalEmBTCParaCompra.getText().toString()),
                            new BigDecimal(inpValorCoinBuy.getText().toString())));

                    txtTotalCoinAdquirido.setText(coinsOptidoPelaCompra.toString());

                    BigDecimal btcOptidoPelaVenda = calcularVenda(new Double(coinsOptidoPelaCompra.toString()), new BigDecimal(inpValorCoinSell.getText().toString()));

                    txtTotalBTCAdquirido.setText(btcOptidoPelaVenda.toString());

                    if (btcOptidoPelaVenda.compareTo(new BigDecimal(inpTotalEmBTCParaCompra.getText().toString())) == -1) {
                        txtTotalBTCAdquirido.setBackgroundColor(Color.RED);
                    } else {
                        txtTotalBTCAdquirido.setBackgroundColor(Color.GREEN);
                    }

                }
            }
        });

        alert.setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });

        dialog = alert.create();
        dialog.setIcon(R.drawable.ic_play_circle_outline_black_24dp);
        dialog.show();
    }

    /**
     * Cria a notificação pra moeda selecionada
     */
    private void showDialogCriarNotificacao() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);
        AlertDialog dialog;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_notificacao, null);
        alert.setView(dialogView);

        inpValorAvisoBuyAbaixo = dialogView.findViewById(R.id.inpValorAvisoBuyInferior);
        inpValorAvisoBuyAcima = dialogView.findViewById(R.id.inpValorAvisoBuySuperior);
        inpValorAvisoSellAbaixo = dialogView.findViewById(R.id.inpValorAvisoSellInferior);
        inpValorAvisoSellAcima = dialogView.findViewById(R.id.inpValorAvisoSellSuperior);
        inpValorDeCompra = dialogView.findViewById(R.id.inpValorDeCompra);
        TextView txtAvisoDeTaxa = dialogView.findViewById(R.id.txtAvisoDeTaxa);
        tbBuyAbaixo = dialogView.findViewById(R.id.tbBuyInferior);

        txtAvisoDeTaxa.setText("AVISO DE TAXA - " + ticker.getSigla());

        tbBuyAbaixo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tbBuyAbaixo.isChecked()) {
                    tbBuyAbaixo.setBackgroundColor(Color.GREEN);
                } else {
                    tbBuyAbaixo.setBackgroundColor(Color.RED);
                }
            }
        });

        tbBuyAcima = dialogView.findViewById(R.id.tbBuySuperior);
        tbBuyAcima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (tbBuyAcima.isChecked()) {
                    tbBuyAcima.setBackgroundColor(Color.GREEN);
                } else {
                    tbBuyAcima.setBackgroundColor(Color.RED);
                }
            }
        });

        tbSellAbaixo = dialogView.findViewById(R.id.tbSellInferior);
        tbSellAbaixo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tbSellAbaixo.isChecked()) {
                    tbSellAbaixo.setBackgroundColor(Color.GREEN);
                } else {
                    tbSellAbaixo.setBackgroundColor(Color.RED);

                }
            }
        });

        tbSellAcima = (ToggleButton) dialogView.findViewById(R.id.tbSellSuperior);
        tbSellAcima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tbSellAcima.isChecked()) {
                    tbSellAcima.setBackgroundColor(Color.GREEN);
                } else {
                    tbSellAcima.setBackgroundColor(Color.RED);

                }
            }
        });


        //Ja tem aviso salvo Buy Inferior?
        if (ticker.getAvisoBuyInferior().compareTo(new BigDecimal("0.0")) == 1) {
            tbBuyAbaixo.setBackgroundColor(Color.GREEN);
            tbBuyAbaixo.setChecked(true);
            inpValorAvisoBuyAbaixo.setText(ticker.getAvisoBuyInferior().toString());
        } else {
            tbBuyAbaixo.setBackgroundColor(Color.RED);
        }

        //Ja tem aviso salvo Buy Superior?
        if (ticker.getAvisoBuySuperior().compareTo(new BigDecimal("0.0")) == 1) {
            tbBuyAcima.setBackgroundColor(Color.GREEN);
            tbBuyAcima.setChecked(true);
            inpValorAvisoBuyAcima.setText(ticker.getAvisoBuySuperior().toString());
        } else {
            tbBuyAcima.setBackgroundColor(Color.RED);
        }

        //Ja tem aviso salvo Sell Inferior?
        if (ticker.getAvisoStopLoss().compareTo(new BigDecimal("0.0")) == 1) {
            tbSellAbaixo.setBackgroundColor(Color.GREEN);
            tbSellAbaixo.setChecked(true);
            inpValorAvisoSellAbaixo.setText(ticker.getAvisoStopLoss().toString());
        } else {
            tbSellAbaixo.setBackgroundColor(Color.RED);
        }

        //Ja tem aviso salvo Sell Superior?
        if (ticker.getAvisoStopGain().compareTo(new BigDecimal("0.0")) == 1) {
            tbSellAcima.setBackgroundColor(Color.GREEN);
            tbSellAcima.setChecked(true);
            inpValorAvisoSellAcima.setText(ticker.getAvisoStopGain().toString());

        } else {
            tbSellAcima.setBackgroundColor(Color.RED);

        }

        BigDecimal valorDeCompra = ticker.getValorDeCompra();
        if(valorDeCompra.compareTo(BigDecimal.ZERO)==0){
            inpValorDeCompra.setText("");
        }else{
            inpValorDeCompra.setText(valorDeCompra.toString());
        }


        alert.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                boolean flagBuyInferior = false;
                boolean flagBuySuperior = false;
                boolean flagSellInferior = false;
                boolean flagSellSuperior = false;


                if(inpValorDeCompra.getText().length()==0){
                   ticker.setValorDeCompra(BigDecimal.ZERO.setScale(8));
                }else{
                    ticker.setValorDeCompra(new BigDecimal(inpValorDeCompra.getText().toString().trim()));
                }

                //Se está checado então tem que possuir dado
                if (tbBuyAbaixo.isChecked()) {

                    if (inpValorAvisoBuyAbaixo.getText().toString().length() > 0) {
                        ticker.setAvisoBuyInferior(new BigDecimal(inpValorAvisoBuyAbaixo.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "Digite um valor para o Aviso de Compra", Toast.LENGTH_LONG).show();
                        flagBuyInferior = true;
                    }
                } else {
                    ticker.setAvisoBuyInferior(new BigDecimal("0.0"));
                }

                if (tbBuyAcima.isChecked()) {

                    if (inpValorAvisoBuyAcima.getText().toString().length() > 0) {
                        ticker.setAvisoBuySuperior(new BigDecimal(inpValorAvisoBuyAcima.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "Digite um valor para o Aviso de Compra", Toast.LENGTH_LONG).show();
                        flagBuySuperior = true;
                    }
                } else {
                    ticker.setAvisoBuySuperior(new BigDecimal("0.0"));
                }

                if (tbSellAbaixo.isChecked()) {

                    if (inpValorAvisoSellAbaixo.getText().toString().length() > 0) {
                        ticker.setAvisoStopLoss(new BigDecimal(inpValorAvisoSellAbaixo.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "Digite um valor para o Aviso de Venda", Toast.LENGTH_LONG).show();
                        flagSellInferior = true;
                    }

                } else {
                    ticker.setAvisoStopLoss(new BigDecimal("0.0"));
                }

                if (tbSellAcima.isChecked()) {

                    if (inpValorAvisoSellAcima.getText().toString().length() > 0) {
                        ticker.setAvisoStopGain(new BigDecimal(inpValorAvisoSellAcima.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "Digite um valor para o Aviso de Venda", Toast.LENGTH_LONG).show();
                        flagSellSuperior = true;
                    }

                } else {
                    ticker.setAvisoStopGain(new BigDecimal("0.0"));
                }


                if (flagBuyInferior && flagBuySuperior && flagSellInferior && flagSellSuperior) {

                    Toast.makeText(getApplicationContext(), "Digite um valor", Toast.LENGTH_LONG).show();
                }

                long retorno = tickerDAO.update(ticker);

                if (retorno == 0)
                    Toast.makeText(MainActivityDrawer.this, "Não foi possível executar a operação", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivityDrawer.this, "Notificação atualizada com sucesso", Toast.LENGTH_SHORT).show();

            }
        })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        return;
                    }
                });


        dialog = alert.create();
        dialog.setIcon(R.drawable.ic_add_alert_black_24dp);
        dialog.show();

    }

    /**
     * @param ticker -
     * @return - Uma String com o texto para o Hint pra Compra
     */
    private String getHintTaxaCompra(Ticker ticker) {
        //Colocando os hint
        StringBuilder hint = new StringBuilder();
        hint.append("Taxa ");
        hint.append(ticker.getSigla());
        hint.append(" para Compra");
        return hint.toString();
    }

    /**
     * @param ticker -
     * @return - Uma String com o texto para o Hint pra Venda
     */
    private String getHintTaxaVenda(Ticker ticker) {
        //Colocando os hint
        StringBuilder hint = new StringBuilder();
        hint.append("Taxa ");
        hint.append(ticker.getSigla());
        hint.append(" para Venda");
        return hint.toString();
    }

    /**
     * @param ticker -
     * @return - Uma String com o texto para o Hint Total Adiquirido
     */
    private String getHintTotalAdiquirido(Ticker ticker) {
        //Colocando os hint
        StringBuilder hint = new StringBuilder();
        hint.append("Total de ");
        hint.append(ticker.getSigla());
        hint.append(" adquirido");
        return hint.toString();
    }

    /**
     * @param totalEmBTC Valor em que se deseja aplicar
     * @return uma String com a quantidade de Altcoin
     */
    private String calcularCompra(BigDecimal totalEmBTC, BigDecimal taxaBuy) {

        //formato de retorno
        DecimalFormat df = new DecimalFormat("#.########");

        if (!(totalEmBTC.compareTo(new BigDecimal(0.0)) == 1)) {
            return "0.00000000";
        }

        BigDecimal buyTemp = taxaBuy;
        //Converte para Double para efetuar o calculo
        Double doubleTaxaBuy = new Double(buyTemp.toString());

        //Converte para Dougle para efetuar o calculo
        Double doubleTotalEmBTC = new Double(totalEmBTC.toString());

        Double totalEmAltCoin;

        totalEmAltCoin = doubleTotalEmBTC / doubleTaxaBuy;
        totalEmAltCoin -= totalEmAltCoin * TAXA_BUY;

        String retorno = df.format(totalEmAltCoin).replace(",", ".");

        return retorno;

    }

    /**
     * Simula um venda de AltCoin
     *
     * @param
     * @return - Um BigDecimal com o valor da venda
     */
    private BigDecimal calcularVenda(Double valorEmAltCoin, BigDecimal taxaSell) {

        //formato de retorno
        DecimalFormat df = new DecimalFormat("#.########");

        BigDecimal sellTemp = taxaSell;

        //converte em Double para efetuar o Calculo
        Double doubleTaxaSell = new Double(sellTemp.toString());

        BigDecimal totalAltCoins;
        Double doubleTotalSellAltCoins;

        doubleTotalSellAltCoins = valorEmAltCoin * doubleTaxaSell;
        doubleTotalSellAltCoins -= doubleTotalSellAltCoins * TAXA_SELL;

        String retorno = df.format(new Double(doubleTotalSellAltCoins.toString()));

        totalAltCoins = new BigDecimal(retorno.replace(",", "."));

        return totalAltCoins;

    }


    private synchronized void atualizarListView(LinkedList<Ticker> tickers) {

        if (adapterExchanges == null) {
            adapterExchanges = new AdapterExchanges(MainActivityDrawer.this, tickers);
            lstTicker.setAdapter(adapterExchanges);

        } else {
            adapterExchanges.clear();
            adapterExchanges.addAll(tickers);
            adapterExchanges.notifyDataSetChanged();
        }


    }


    /**
     * Adiciona uma nova moeda
     */
    private void showDialogAdicionarMoeda() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cadastrar_coin, null);
        alert.setView(dialogView);

        final EditText inpSiglaMoeda = dialogView.findViewById(R.id.inpSiglaMoeda);
        TextView txtAdicionar = dialogView.findViewById(R.id.txtAdicionar);


        txtAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //verifica se a sigla não esta vazia
                if (inpSiglaMoeda.getText().length() < 1) {
                    Toast.makeText(MainActivityDrawer.this, "Campo Sigla esta vazio.", Toast.LENGTH_LONG).show();
                    return;
                }

                Ticker t = new Ticker();
                t.setSigla(inpSiglaMoeda.getText().toString());
                String nomeExchange = mapSiglaNomeExchange.get(t.getSigla());


                if (nomeExchange == null) {
                    Toast.makeText(MainActivityDrawer.this, "Sigla inválida", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (nomeExchange.toUpperCase().equals("BTC")) {
                        Toast.makeText(MainActivityDrawer.this, "Sigla inválida", Toast.LENGTH_LONG).show();
                        return;
                    }
                    t.setNomeExchange(nomeExchange);
                    t.setUrlApi(WebServiceUtil.getUrl() + t.getSigla().toLowerCase());
                }

                try {
                    dialog = new ProgressDialog(MainActivityDrawer.this);
                    dialog.setMessage("Processando...");
                    dialog.setTitle("BittrexAnalizer");
                    dialog.show();

                    //Salva no BD
                    t.setId(tickerDAO.create(t));

                    //Se salvou, atualiza
                    if (t.getId() != null) {
                        Toast.makeText(MainActivityDrawer.this, "Salvo com Sucesso", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivityDrawer.this, "Não foi possível executar a operação", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Log.i(LOG, e.getMessage());
                } finally {
                    dialog.dismiss();
                    alertDialog.cancel();
                    getTicker();
                }
            }
        });


        alert.setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });

        alertDialog = alert.create();
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);
        alertDialog.show();

    }

    /**
     * Deleta a moeda Selecionada
     */
    private void showDialogDeletarMoeda() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_deletar_coin, null);
        alert.setView(dialogView);

        final EditText inpSiglaMoeda = dialogView.findViewById(R.id.inpSiglaMoeda);
        final EditText inpNomeMoeda = dialogView.findViewById(R.id.inpNomeMoeda);
        final TextView txtDeletar = dialogView.findViewById(R.id.txtDeletar);

        inpSiglaMoeda.setText(ticker.getSigla());
        inpNomeMoeda.setText(ticker.getNomeExchange());

        txtDeletar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                try {
                    dialog = new ProgressDialog(MainActivityDrawer.this);
                    dialog.setMessage("Processando...");
                    dialog.setTitle("BittrexAnalizer");
                    dialog.show();

                    //Deleta do BD
                    tickerDAO.delete(ticker);

                    Toast.makeText(MainActivityDrawer.this, "Deletado com sucesso", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.i(LOG, e.getMessage());
                } finally {
                    dialog.dismiss();
                    alertDialog.cancel();
                    getTicker();
                }

            }
        });

        alert.setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });

        alertDialog = alert.create();
        alertDialog.setIcon(R.drawable.ic_delete_black_24dp);
        alertDialog.show();

    }

    private void showDialogDesligarNotificacoes() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmar_desligar_notificacoes, null);
        alert.setView(dialogView);

        final TextView txtDeligarNotificacao = dialogView.findViewById(R.id.txtDeligarNotificacao);

        txtDeligarNotificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopServiceTaxa();
                Toast.makeText(MainActivityDrawer.this, "Desligado o Serviço de Notificações", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });

        alertDialog = alert.create();
        alertDialog.setIcon(R.drawable.ic_alarm_off_black_24dp);
        alertDialog.show();

    }

    @Override
    public void run() {

        try {
            mapSiglaNomeExchange = HttpClient.findAllCurrencies();
            SessionUtil.getInstance().setNomeExchanges(mapSiglaNomeExchange);
        } catch (Exception e) {
            Log.i(LOG, e.getMessage());
        }
    }


    /**
     * Método que atualiza a lista de 30 em 30 segundos
     */
    private class MyTask extends TimerTask {

        @Override
        public void run() {
            try {

                getTicker();

                if(SessionUtil.getInstance().getMapBalances().containsKey("BTC")) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (new Boolean(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ROBOT_LIGADO))) {

                                BalanceStrategy.execute();

                                //Se não tiver valor será stopado o Service Compra
                                Balance b = SessionUtil.getInstance().getMapBalances().get("BTC");
                                BigDecimal valorCompraRobot = new BigDecimal(SessionUtil.getInstance().getMapConfiguracao()
                                        .get(ConstantesUtil.VALOR_COMPRA_ROBOT)).setScale(8);
                                if (b.getBalance().compareTo(valorCompraRobot) == -1) {
                                    stopServiceCompra();
                                } else {
                                    startServiceCompra();
                                }
                            } else {
                                stopServiceCompra();
                            }
                        }
                    });
                    t.start();
                }

            } catch (Exception e) {
                Log.i(LOG, e.getMessage());
            }
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {

        } else if (id == R.id.action_setttings) {
            Intent i = new Intent(this, UserConfiguracao.class);
            startActivity(i);
        } else if (id == R.id.action_execute) {


            AlarmAnalizerCompraStrategy alarmAnalizerCompraStrategy = new AlarmAnalizerCompraStrategy();
            alarmAnalizerCompraStrategy.executar(MainActivityDrawer.this);

        } else if (id == R.id.action_calcular_porcentagem) {

            showCalcularPorcentagemDialog();

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        final String NOME_ASC = "NOME ASC";
        final String NOME_DESC = "NOME DESC";
        final String LAST_ASC = "LAST ASC";
        final String LAST_DESC = "LAST DESC";

        if (id == R.id.nav_add_coin) {
            showDialogAdicionarMoeda();
        } else if (id == R.id.nav_notification) {
            showDialogDesligarNotificacoes();
        } else if (id == R.id.nav_clas_nome_moeda) {

            if (tickers == null || tickers.size() == 0) {
                Toast.makeText(MainActivityDrawer.this, "A Lista esta vazia", Toast.LENGTH_LONG).show();
                return true;
            }

            if (contadorNomeClassificacao % 2 == 0)
                Collections.sort(tickers);
            else
                Collections.reverse(tickers);

            contadorNomeClassificacao++;

        } else if (id == R.id.nav_clas_last) {

            if (tickers == null || tickers.size() == 0) {
                Toast.makeText(MainActivityDrawer.this, "A Lista esta vazia", Toast.LENGTH_LONG).show();
                return true;
            }


            if (contadorLastClassificacao % 2 == 0)
                tickers = ordenar(LAST_ASC, tickers);
            else
                tickers = ordenar(LAST_DESC, tickers);

            contadorLastClassificacao++;

        } else if (id == R.id.nav_open_orders) {
            Intent i = new Intent(this, UserOpenOrders.class);
            startActivity(i);

        } else if (id == R.id.nav_orders) {
            Intent i = new Intent(this, UserOrders.class);
            startActivity(i);

        } else if (id == R.id.nav_balances) {
            Intent i = new Intent(this, UserBalances.class);
            startActivity(i);
        } else if (id == R.id.nav_keys) {
            Intent i = new Intent(this, UserKey.class);
            startActivity(i);
        } else if (id == R.id.nav_start_service_compra) {
            startServiceCompra();
        } else if (id == R.id.nav_stop_service_compra) {
            stopServiceCompra();
        } else if (id == R.id.nav_start_service_venda) {
            startServiceVenda();
        } else if (id == R.id.nav_stop_service_venda) {
            stopServiceVenda();
        }

        verificarMenuService();

        atualizarListView(tickers);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showCalcularPorcentagemDialog() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityDrawer.this);
        AlertDialog dialog;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_calcular_porcentagem, null);
        alert.setView(dialogView);

        final EditText inpValorAplicado = dialogView.findViewById(R.id.inpValorAplicado);
        final EditText inpPorcentagem = dialogView.findViewById(R.id.inpPorcentagem);
        final TextView txtResutado = dialogView.findViewById(R.id.txtResultado);
        final ImageButton imgCalcular = dialogView.findViewById(R.id.imgCalcular);


        //Setando os hints

        imgCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                BigDecimal porcentagem = new BigDecimal(inpPorcentagem.getText().
                        toString()).setScale(2);
                BigDecimal valorAplicado = new BigDecimal(inpValorAplicado.getText().toString());

                txtResutado.setText(CalculoUtil.getPorcentagem(valorAplicado, porcentagem).toString());

            }
        });

        alert.setNegativeButton("SAIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });

        dialog = alert.create();
        dialog.setIcon(R.drawable.ic_play_circle_outline_black_24dp);
        dialog.show();
    }

}


package br.com.bittrexanalizer.telas;

/**
 * Created by PauLinHo on 25/12/2017.
 */

interface IFlagment {

    String CADASTRAR = "Cadastrar";
    String EDITAR = "Editar";
    String DELETAR = "Deletar";

    long TEMPO_SLEEP = 700;

    String MENSAGEM_DIALOG = "Escolha a Opção";

    void atualizarListView();

}


package br.com.bittrexanalizer.receivers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import br.com.bittrexanalizer.facade.CompraFacade;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 09/02/2018.
 */

public class ServiceCompra extends Service {

    private int startId;
    public static boolean ativo = false;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startId = -1;
        if (ativo) {
            return;
        }
        ativo = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (ativo) {

                    try {
                        Thread.sleep(660000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    context = getBaseContext();

                    CompraFacade compraFacade = new CompraFacade();
                    compraFacade.executar(context);

                    long tempoEnvioEmail = Long.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ROBOT_TEMPO_ENVIO_EMAIL));
                    if (System.currentTimeMillis() >= (SessionUtil.getInstance().getUltimoHorarioSalvo() + tempoEnvioEmail)) {
                        enviarEmail();
                    }


                }
            }
        });
        t.start();


        return START_STICKY;
    }

    private void enviarEmail() {

        EmailUtil emailUtil = new EmailUtil();

        emailUtil.enviarEmail(context, SessionUtil.getInstance().getMsgErros().toString(), "ROBOT ERROS");

        SessionUtil.getInstance().setMsgErros(new StringBuilder());
        SessionUtil.getInstance().setUltimoHorarioSalvo(System.currentTimeMillis());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopSelf(startId);
        ativo = false;

    }
}


package br.com.bittrexanalizer.receivers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import br.com.bittrexanalizer.facade.VendaFacade;

/**
 * Created by PauLinHo on 09/02/2018.
 */

public class ServiceVenda extends Service {
    private int startId;
    private boolean ativo = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startId = -1;
        if(ativo){
            return;
        }
        ativo = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (ativo) {

                    Context context = getBaseContext();

                    VendaFacade vendaFacade = new VendaFacade();
                    vendaFacade.executar(context);

                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t.start();


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        ativo = false;

        stopSelf(startId);

    }
}


package br.com.bittrexanalizer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by PauLinHo on 09/02/2018.
 */

public class ReceiverBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent = new Intent(context, ServiceCompra.class);
        context.startService(intent);

        intent = new Intent(context, ServiceVenda.class);
        context.startService(intent);

    }
}


package br.com.bittrexanalizer.receivers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import br.com.bittrexanalizer.facade.AlarmTaxaFacade;

/**
 * Created by PauLinHo on 09/02/2018.
 */
public class ServiceAvisoTaxa extends Service {
    private int startId;
    private boolean ativo = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startId = -1;
        if(ativo){
            return;
        }
        ativo = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (ativo) {

                    Context context = getBaseContext();

                    AlarmTaxaFacade alarmTaxaFacade = new AlarmTaxaFacade();
                    alarmTaxaFacade.execute(context);

                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t.start();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ativo = false;

        stopSelf(startId);

    }
}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Configuracao;


/**
 * Created by PauLinHo on 10/08/2017.
 */

public class AdapterConfiguracoes extends ArrayAdapter<Configuracao> {

    private Context context;
    private List<Configuracao> lista;

    public AdapterConfiguracoes(Context context, List<Configuracao> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Configuracao configuracao = new Configuracao();
        configuracao = this.lista.get(position);

        convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_item_configuracao, null);

        TextView txtConfiguracaoPropriedade = (TextView) convertView.findViewById(R.id.txtConfiguracaoPropriedade);
        TextView txtConfiguracaoValor = (TextView) convertView.findViewById(R.id.txtConfiguracaoValor);


        txtConfiguracaoPropriedade.setText(configuracao.getPropriedade());
        txtConfiguracaoValor.setText(configuracao.getValor());

        return convertView;
    }

}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Order;


/**
 * Created by PauLinHo on 10/08/2017.
 */

public class AdapterOpenOrders extends ArrayAdapter<Order> {

    private Context context;
    private List<Order> lista;

    public AdapterOpenOrders(Context context, List<Order> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Order order = new Order();
        order = this.lista.get(position);

        convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_item_open_orders, null);

        if(position%2==0){
            convertView.setBackgroundColor(Color.LTGRAY);
        }else{
            convertView.setBackgroundColor(Color.WHITE);
        }

        TextView txtCurrency = convertView.findViewById(R.id.txtCurrency);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        TextView txtOpened = convertView.findViewById(R.id.txtOpened);
        TextView txtCondition = convertView.findViewById(R.id.txtCondition);
        TextView txtOrderType = convertView.findViewById(R.id.txtOrderType);
        TextView txtLimit = convertView.findViewById(R.id.txtLimit);


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        txtCurrency.setText(order.getExchange());
        txtQuantity.setText(order.getQuantity().toString());
        txtCondition.setText(order.getCondition().toString());
        txtOpened.setText(sdf.format(order.getOpened().getTime()));
        txtOrderType.setText(order.getOrderType());
        txtLimit.setText(order.getLimit().toString());

        return convertView;

    }

}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Order;


/**
 * Created by PauLinHo on 10/08/2017.
 */

public class AdapterOrder extends ArrayAdapter<Order> {

    private Context context;
    private List<Order> lista;

    public AdapterOrder(Context context, List<Order> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Order order = new Order();
        order = this.lista.get(position);

        convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_item_orders, null);

        if(position%2==0){
            convertView.setBackgroundColor(Color.LTGRAY);
        }else{
            convertView.setBackgroundColor(Color.WHITE);
        }

        TextView txtExchange = convertView.findViewById(R.id.txtExchange);
        TextView txtOrderType = convertView.findViewById(R.id.txtOrderType);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        TextView txtTimeStamp = convertView.findViewById(R.id.txtTimeStamp);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        txtExchange.setText(order.getExchange());
        txtOrderType.setText(order.getOrderType());
        txtQuantity.setText(order.getQuantity().toString());
        txtPrice.setText(order.getPrice().toString());
        txtTimeStamp.setText(sdf.format(order.getTimeStamp().getTime()));


        return convertView;

    }

}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Balance;


/**
 * Created by PauLinHo on 10/08/2017.
 */

public class AdapterBalances extends ArrayAdapter<Balance> {

    private Context context;
    private List<Balance> lista;

    public AdapterBalances(Context context, List<Balance> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Balance balance = new Balance();
        balance = this.lista.get(position);

        convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_item_balance, null);

        if(position%2==0){
            convertView.setBackgroundColor(Color.LTGRAY);
        }else{
            convertView.setBackgroundColor(Color.WHITE);
        }

        TextView txtCurrency = convertView.findViewById(R.id.txtCurrency);
        TextView txtBalance = convertView.findViewById(R.id.txtBalance);
        TextView txtAvailable = convertView.findViewById(R.id.txtAvailable);
        TextView txtPending = convertView.findViewById(R.id.txtPending);


        txtCurrency.setText(balance.getCurrency());
        txtBalance.setText(balance.getBalance().toString());
        txtAvailable.setText(balance.getAvailable().toString());
        txtPending.setText(balance.getPending().toString());

        return convertView;

    }

}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.domain.Ticker;

/**
 * Created by PauLinHo on 08/10/2017.
 */

public class AdapterExchanges extends ArrayAdapter<Ticker> {

    private List<Ticker> lista;
    private Context context;

    public AdapterExchanges(Context context, List<Ticker> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Ticker ticker = new Ticker();
        ticker = lista.get(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.activity_item_ticker, null);

        TextView txtNomeMoeda = (TextView) convertView.findViewById(R.id.txtNomeMoeda);
        TextView txtSiglaMoeda = (TextView) convertView.findViewById(R.id.txtSiglaMoeda);
        TextView txtLast = (TextView) convertView.findViewById(R.id.txtLast);
        TextView txtBid = (TextView) convertView.findViewById(R.id.txtBid);
        TextView txtAsk = (TextView) convertView.findViewById(R.id.txtAsk);

        txtNomeMoeda.setText(ticker.getNomeExchange());
        txtSiglaMoeda.setText(ticker.getSigla());
        txtLast.setText(ticker.getLast().toString());
        txtBid.setText(ticker.getBid().toString());
        txtAsk.setText(ticker.getAsk().toString());

        return convertView;
    }

}


package br.com.bittrexanalizer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.api.ApiCredentials;


/**
 * Created by PauLinHo on 10/08/2017.
 */

public class AdapterKeys extends ArrayAdapter<ApiCredentials> {

    private Context context;
    private List<ApiCredentials> lista;

    public AdapterKeys(Context context, List<ApiCredentials> lista) {
        super(context, 0, lista);
        this.context = context;
        this.lista = new ArrayList<>();
        this.lista = lista;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ApiCredentials apiCredentials = new ApiCredentials();
        apiCredentials = this.lista.get(position);

        convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_item_key, null);

        TextView txtID = convertView.findViewById(R.id.txtId);
        TextView txtKey = convertView.findViewById(R.id.txtKey);
        TextView txtSecret = convertView.findViewById(R.id.txtSecret);


        txtID.setText(apiCredentials.getId().toString());
        txtKey.setText(apiCredentials.getKey());
        txtSecret.setText(apiCredentials.getSecret());

        return convertView;
    }

}


package br.com.bittrexanalizer.webserver;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import br.com.bittrexanalizer.domain.MarketHistory;
import br.com.bittrexanalizer.domain.Ticker;

/**
 * Created by PauLinHo on 10/09/2017.
 */

public class HttpClient {

    private static Gson gson = null;
    private static StringBuilder retorno = null;

    private static volatile Ticker ticker = null;
    private static MarketHistory marketHistory = null;
    private static final String urlAllCurrencies = "https://bittrex.com/api/v1.1/public/getcurrencies";
    private static final String GET_MARTKET_SUMMARY = "https://bittrex.com/api/v1.1/public/getmarketsummary?market=btc-";
    private static final String GET_MARKET_HISTORY = "https://bittrex.com/api/v1.1/public/getmarkethistory?market=btc-";


    /**
     * @param enderecoURL - um endereço URL
     * @return - Um String Gson com a entidade encontrada ou NULL
     */
    public synchronized static Ticker find(String enderecoURL) {

        String output = null;

        ticker = new Ticker();

        StringBuffer temp = new StringBuffer();
        temp.append(enderecoURL);

        try {
            URL url = new URL(temp.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : Http error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            retorno = new StringBuilder();

            while ((output = br.readLine()) != null) {
                retorno.append(output);
            }

            ticker = getTicker(retorno.toString());

            conn.disconnect();

        } catch (Exception e) {
            Log.d("ERRO", e.getMessage());
            return null;
        }

        return ticker;
    }

    public static Map<String, String> findAllCurrencies() {

        String output = null;

        Map<String, String> mapString = new HashMap<>();

        StringBuffer temp = new StringBuffer();
        temp.append(urlAllCurrencies);

        try {
            URL url = new URL(temp.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : Http error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            retorno = new StringBuilder();

            while ((output = br.readLine()) != null) {
                retorno.append(output);
            }

            mapString = getAllCurrencies(retorno.toString());

            conn.disconnect();

        } catch (Exception e) {
            Log.d("ERRO", e.getMessage());
            return null;
        }

        return mapString;
    }


    private static synchronized Ticker getTicker(String dados) {

        DecimalFormat df = new DecimalFormat("#.########");

        String[] temp = dados.split("\\{");
        String[] temp2 = temp[2].split(",");
        for (int i = 0; i < temp2.length; i++) {
            temp2[i] = temp2[i].replace("\"", "");
            temp2[i] = temp2[i].replace("{", "");
            temp2[i] = temp2[i].replace("}", "");
            String str[] = temp2[i].split(":");


            switch (str[0].trim()) {
                case "Bid":
                    ticker.setBid(new BigDecimal(str[1].trim()));
                    break;
                case "Ask":
                    ticker.setAsk(new BigDecimal(str[1].trim()));
                    break;
                case "Last":
                    ticker.setLast(new BigDecimal(str[1].trim()));
                    break;
            }


        }
        return ticker;
    }

    private static Map<String, String> getAllCurrencies(String dados) {

        Map<String, String> mapRetorno = new HashMap<>();
        String key = "";
        String valor = "";

        String[] temp = dados.split("\\{");
        for (int j = 2; j < temp.length; j++) {
            String[] temp2 = temp[j].split(",");
            for (int i = 0; i < temp2.length; i++) {
                temp2[i] = temp2[i].replace("\"", "");
                temp2[i] = temp2[i].replace("{", "");
                temp2[i] = temp2[i].replace("}", "");
                String str[] = temp2[i].split(":");

                switch (str[0].trim()) {
                    case "Currency":
                        key = str[1].trim();
                        break;
                    case "CurrencyLong":
                        mapRetorno.put(key, str[1].trim());
                        break;
                    case "IsActive":
                        //moeda não esta ativa
                        if (str[1].toLowerCase().equals("false")) {
                            mapRetorno.remove(key);
                        }
                        break;
                }
            }


        }
        return mapRetorno;
    }

    public static Ticker getMarketSummary(String siglaMoeda) {

        String output = null;

        ticker = new Ticker();

        StringBuffer temp = new StringBuffer();
        temp.append(GET_MARTKET_SUMMARY);
        temp.append(siglaMoeda);

        try {
            URL url = new URL(temp.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : Http error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            retorno = new StringBuilder();

            while ((output = br.readLine()) != null) {
                retorno.append(output);
            }

            ticker = getTickerFull(retorno.toString());

            conn.disconnect();

        } catch (Exception e) {
            Log.d("ERRO", e.getMessage());
            return null;
        }

        return ticker;
    }

    public static MarketHistory getMarketHistory(String siglaMoeda) {

        String output = null;

        marketHistory = new MarketHistory();

        StringBuffer temp = new StringBuffer();
        temp.append(GET_MARKET_HISTORY);
        temp.append(siglaMoeda);

        try {
            URL url = new URL(temp.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : Http error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            retorno = new StringBuilder();

            while ((output = br.readLine()) != null) {
                retorno.append(output);
            }

            marketHistory = getMarketHistorySplit(retorno.toString());

            conn.disconnect();

        } catch (Exception e) {
            Log.d("ERRO", e.getMessage());
            return null;
        }

        return marketHistory;
    }


    private static Ticker getTickerFull(String dados) {

        DecimalFormat df = new DecimalFormat("#.########");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        String[] temp = dados.split("\\{");
        String[] temp2 = temp[2].split(",");
        for (int i = 0; i < temp2.length; i++) {
            temp2[i] = temp2[i].replace("\"", "");
            temp2[i] = temp2[i].replace("{", "");
            temp2[i] = temp2[i].replace("}", "");
            String str[] = temp2[i].split(":");

            switch (str[0].trim()) {
                case "Bid":
                    ticker.setBid(new BigDecimal(str[1].trim()));
                    break;
                case "Ask":
                    ticker.setAsk(new BigDecimal(str[1].trim()));
                    break;
                case "Last":
                    ticker.setLast(new BigDecimal(str[1].trim()));
                    break;

            }


        }
        return ticker;
    }

    private static MarketHistory getMarketHistorySplit(String dados) {

        DecimalFormat df = new DecimalFormat("#.########");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String[] temp = dados.split("\\{");
        String[] temp2 = temp[2].split(",");
        for (int i = 0; i < temp2.length; i++) {
            temp2[i] = temp2[i].replace("\"", "");
            temp2[i] = temp2[i].replace("{", "");
            temp2[i] = temp2[i].replace("}", "");
            String str[] = temp2[i].split(":");

            /*"Id" : 319435,
                    "TimeStamp" : "2014-07-09T03:21:20.08",
                    "Quantity" : 0.30802438,
                    "Price" : 0.01263400,
                    "Total" : 0.00389158,
                    "FillType" : "FILL",
                    "OrderType" : "BUY"*/

            switch (str[0].trim()) {
                case "Id":
                    marketHistory.setId(Long.valueOf(str[1].trim()));
                    break;
                case "Quantity":
                    marketHistory.setQuantity(new BigDecimal(str[1].trim()));
                    break;
                case "Price":
                    marketHistory.setPrice(new BigDecimal(str[1].trim()));
                    break;
                case "Total":
                    marketHistory.setTotal(new BigDecimal(str[1].trim()));
                    break;
                case "FillType":
                    marketHistory.setFillType(str[1].trim());
                    break;
                case "OrderType":
                    marketHistory.setOrderType(str[1].trim());
                    break;
                case "TimeStamp":
                    try {
                        StringBuilder data = new StringBuilder();
                        data.append((str[1].trim().replace("T", " ")));
                        data.append(":");
                        data.append(str[2].trim());
                        data.append(":");
                        String dataSplit[] = str[3].trim().replace(".", ":").split(":");
                        data.append(dataSplit[0]);
                        marketHistory.setTimeStamp(sdf.parse(data.toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;

            }


        }
        return marketHistory;
    }

    public static String find(String enderecoURL, String API_KEY) {

        String output = null;

        StringBuffer resultBuffer;

        StringBuffer temp = new StringBuffer();
        temp.append(enderecoURL);

        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        //CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            URL url = new URL(temp.toString());

            HttpGet httpGet = new HttpGet();
            if (API_KEY != null) {
                httpGet.addHeader("apisign", API_KEY); // Attaches signature as a header
            }
            httpGet.setURI(new URI(enderecoURL));

            HttpResponse response = client.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            resultBuffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null)

                resultBuffer.append(line);


        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
            return null;
        }

        return resultBuffer.toString();
    }

    public static String findPost(String enderecoURL, String API_KEY, Object dados) {

        String output = null;

        StringBuffer resultBuffer;

        StringBuffer temp = new StringBuffer();
        temp.append(enderecoURL);

        org.apache.http.client.HttpClient client = new DefaultHttpClient();

        try {
            URL url = new URL(temp.toString());

            HttpPost httpPost = new HttpPost();
            if (API_KEY != null) {
                httpPost.addHeader("apisign", API_KEY); // Attaches signature as a header
           }
           //httpPost.addHeader("__RequestVerificationToken", "LgflegHnn0I-ubbRaB8J0IaWj3w9NzhpqqiEOJeQUUiZfvXwH-A26WJ0oqP8u3tGbT8LVv7ZPSEYviISqUNlc67LCB2gpXWG5u-doPXBTEBiXPs0bEJYQ2F8YKlFkLGnSFkv6A2");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setURI(new URI(enderecoURL));

            Gson gson = new Gson();
            String data = gson.toJson(dados);
//
            Log.i("Bitttrex", data);
//            HttpEntity entity = new StringEntity(data);
//            httpPost.setEntity(entity);

            HttpResponse response = client.execute(httpPost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            resultBuffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null)

                resultBuffer.append(line);


        } catch (Exception e) {
            Log.i("BITTREX", e.getMessage());
            return null;
        }

        return resultBuffer.toString();
    }

}


package br.com.bittrexanalizer.api;


import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import br.com.bittrexanalizer.utils.HexUtil;
import br.com.bittrexanalizer.utils.SessionUtil;


public class EncryptionUtility {

    public final static String algorithmUsed = "HmacSHA512";

    public static String calculateHash(String url, String algorithm) {

        Mac shaHmac = null;

        try {

            shaHmac = Mac.getInstance(algorithm);

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        SecretKeySpec secretKey = new SecretKeySpec(SessionUtil.getInstance().getApiCredentials().getSecret().getBytes(), algorithm);

        try {

            shaHmac.init(secretKey);

        } catch (InvalidKeyException e) {

            e.printStackTrace();
        }

        byte[] hash = shaHmac.doFinal(url.getBytes());

        String check = HexUtil.encodeHexString(hash);


        return check;
    }

    public static String generateNonce() {

        SecureRandom random = null;

        try {

            random = SecureRandom.getInstance("SHA1PRNG");

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        random.setSeed(System.currentTimeMillis());

        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);

        String nonce = null;

        nonce = new String(Base64.encode(nonceBytes, Base64.NO_WRAP));

        return nonce;
    }

    private static String hexToASCII(String hexValue)
    {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexValue.length(); i += 2)
        {
            if(i+2<=hexValue.length())
            {
                String str = hexValue.substring(i, i + 2);
                output.append(((char)Integer.parseInt(str,16)));
            }
        }
        System.out.println(output.toString());
        return output.toString();
    }
}


package br.com.bittrexanalizer.api;

import br.com.bittrexanalizer.domain.EntidadeDomain;

public class ApiCredentials extends EntidadeDomain{

	private Long id;
    private String key;
    private String secret;

    public ApiCredentials() {
    }

    public ApiCredentials(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}


package br.com.bittrexanalizer.domain;

import java.math.BigDecimal;
import java.util.Calendar;

public class Order extends EntidadeDomain implements Comparable<Order> {

/*
	AccountId" : null,
	"Uuid" : null,
	"OrderUuid" : "09aa5bb6-8232-41aa-9b78-a5a1093e0211",
	"Exchange" : "BTC-LTC",
	"Type" : "LIMIT_BUY",
	"OrderType" : "LIMIT_SELL",
	"Quantity" : 5.00000000,
	"QuantityRemaining" : 5.00000000,
	"Limit" : 2.00000000,
	"Reserved" : 0.00001000,
	"ReserveRemaining" : 0.00001000,
	"CommissionPaid" : 0.00000000,
	"CommissionReserved" : 0.00000002,
	"CommissionReserveRemaining" : 0.00000002,
	"Price" : 0.00000000,
	"PricePerUnit" : null,
	"Opened" : "2014-07-09T03:55:48.77",
	"Closed" : null,
	"IsOpen" : true,
	"CancelInitiated" : false,
	"ImmediateOrCancel" : false,
	"IsConditional" : false,
	"Condition" : null,
	"Sentinel" : "6c454604-22e2-4fb4-892e-179eede20972",
	"ConditionTarget" : null*/


    /*Closed: "2017-07-23T21:01:04.65",
    Commission: 0.00024937,
    Condition: "NONE",
    ConditionTarget: null,
    Exchange: "BTC-RDD",
    ImmediateOrCancel: false,
    IsConditional: false,
    Limit: 4.8e-7,
    OrderType: "LIMIT_BUY",
    OrderUuid: "44e8751c-3df8-4a75-841b-c7c2145b746b",
    Price: 0.09975,
    PricePerUnit: 4.8e-7,
    Quantity: 207812.5,
    QuantityRemaining: 0,
    TimeStamp: "2017-07-23T18:17:40.387"*/

    private Long id;
    private Long AccountId;
    private String uuid;
    private String orderUuid;
    private String exchange;
    private String sigla;
    private String type;
    private String orderType;
    private BigDecimal quantity;
    private BigDecimal quantityRemaining;
    private BigDecimal rate;
    private BigDecimal reserved;
    private BigDecimal reserveRemaining;
    private BigDecimal limit;
    private BigDecimal comission;
    private BigDecimal comissionPaid;
    private BigDecimal comissionReserved;
    private BigDecimal comissionReservedRemaining;
    private BigDecimal price;
    private BigDecimal pricePerUnit;
    private Calendar timeStamp;
    private Calendar opened;
    private Calendar closed;
    private Boolean isOpen;
    private Boolean cancelInitiated;
    private Boolean immediateOrCancel;
    private Boolean isConditional;
    private String sentinel;
    private String condition;
    private String conditionTarget;
    private String conditionType;
    private String timeInEffect;

    public Order() {
        quantity = BigDecimal.ZERO;
        quantityRemaining = BigDecimal.ZERO;
        limit = BigDecimal.ZERO;
        setComissionPaid(BigDecimal.ZERO);
        comission = BigDecimal.ZERO;
        comissionReserved = BigDecimal.ZERO;
        comissionReservedRemaining = BigDecimal.ZERO;
        reserved = BigDecimal.ZERO;
        reserveRemaining = BigDecimal.ZERO;
        price = BigDecimal.ZERO;
        pricePerUnit = BigDecimal.ZERO;
        setRate(BigDecimal.ZERO);
        timeStamp = Calendar.getInstance();
        opened = Calendar.getInstance();
        closed = Calendar.getInstance();
    }


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getOrderUuid() {
        return orderUuid;
    }


    public void setOrderUuid(String orderUuid) {
        this.orderUuid = orderUuid;
    }


    public String getExchange() {
        return exchange;
    }


    public void setExchange(String exchange) {
        this.exchange = exchange;
    }


    public String getOrderType() {
        return orderType;
    }


    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }


    public BigDecimal getQuantity() {
        return quantity;
    }


    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }


    public BigDecimal getQuantityRemaining() {
        return quantityRemaining;
    }


    public void setQuantityRemaining(BigDecimal quantityRemaining) {
        this.quantityRemaining = quantityRemaining;
    }


    public BigDecimal getLimit() {
        return limit;
    }


    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }


    public BigDecimal getComissionPaid() {
        return comissionPaid;
    }


    public void setComissionPaid(BigDecimal comissionPaid) {
        this.comissionPaid = comissionPaid;
    }


    public BigDecimal getPrice() {
        return price;
    }


    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }


    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }


    public Calendar getOpened() {
        return opened;
    }


    public void setOpened(Calendar opened) {
        this.opened = opened;
    }


    public Calendar getClosed() {
        return closed;
    }


    public void setClosed(Calendar closed) {
        this.closed = closed;
    }


    public Boolean getCancelInitiated() {
        return cancelInitiated;
    }


    public void setCancelInitiated(Boolean cancelInitiated) {
        this.cancelInitiated = cancelInitiated;
    }


    public Boolean getImmediateOrCancel() {
        return immediateOrCancel;
    }


    public void setImmediateOrCancel(Boolean immediateOrCancel) {
        this.immediateOrCancel = immediateOrCancel;
    }


    public Boolean getIsConditional() {
        return getConditional();
    }


    public void setIsConditional(Boolean isConditional) {
        this.setConditional(isConditional);
    }


    public String getCondition() {
        return condition;
    }


    public void setCondition(String condition) {
        this.condition = condition;
    }


    public String getConditionTarget() {
        return conditionTarget;
    }


    public void setConditionTarget(String conditionTarget) {
        this.conditionTarget = conditionTarget;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderUuid == null) ? 0 : orderUuid.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (orderUuid == null) {
            if (other.orderUuid != null)
                return false;
        } else if (!orderUuid.equals(other.orderUuid))
            return false;
        return true;
    }

    @Override
    public int compareTo(Order o) {
        int valor = 0;

        if (this.getOpened().compareTo(o.getOpened()) == 1)
            valor = 1;
        else if (this.getOpened().compareTo(o.getOpened()) == -1)
            valor = -1;

        return valor;
    }


    @Override
    public String toString() {
        return "Order [uuid=" + uuid + ", orderUuid=" + orderUuid + ", exchange=" + exchange + ", orderType="
                + orderType + ", quantity=" + quantity + ", quantityRemaining=" + quantityRemaining + ", limit=" + limit
                + ", comissionPaid=" + getComissionPaid() + ", price=" + price + ", pricePerUnit=" + pricePerUnit
                + ", opened=" + opened + ", closed=" + closed + ", cancelInitiated=" + cancelInitiated
                + ", immediateOrCancel=" + immediateOrCancel + ", isConditional=" + getConditional() + ", condition="
                + condition + ", conditionTarget=" + conditionTarget + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return AccountId;
    }

    public void setAccountId(Long accountId) {
        AccountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getReserveRemaining() {
        return reserveRemaining;
    }

    public void setReserveRemaining(BigDecimal reserveRemaining) {
        this.reserveRemaining = reserveRemaining;
    }

    public BigDecimal getComissionReserved() {
        return comissionReserved;
    }

    public void setComissionReserved(BigDecimal comissionReserved) {
        this.comissionReserved = comissionReserved;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public Boolean getConditional() {
        return isConditional;
    }

    public void setConditional(Boolean conditional) {
        isConditional = conditional;
    }

    public String getSentinel() {
        return sentinel;
    }

    public void setSentinel(String sentinel) {
        this.sentinel = sentinel;
    }

    public BigDecimal getComissionReservedRemaining() {
        return comissionReservedRemaining;
    }

    public void setComissionReservedRemaining(BigDecimal comissionReservedRemaining) {
        this.comissionReservedRemaining = comissionReservedRemaining;
    }

    public BigDecimal getComission() {
        return comission;
    }

    public void setComission(BigDecimal comission) {
        this.comission = comission;
    }

    public Calendar getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Calendar timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getTimeInEffect() {
        return timeInEffect;
    }

    public void setTimeInEffect(String timeInEffect) {
        this.timeInEffect = timeInEffect;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 12/01/2018.
 */

/**
 * Esta classe representa as configurações do sistema,
 * onde pode ser alterado os valores manualmente, assim o sistema entendera
 */
public class Configuracao extends EntidadeDomain{

    private Long id;
    private String propriedade;
    private String valor;

    public Configuracao(Long id, String propriedade, String valor) {
        this(propriedade, valor);
        this.id = id;
    }

    public Configuracao(String propriedade, String valor) {
        this.propriedade = propriedade;
        this.valor = valor;
    }



    public Configuracao(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPropriedade() {
        return propriedade;
    }

    public void setPropriedade(String propriedade) {
        this.propriedade = propriedade;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Configuracao{" +
                "id=" + id +
                ", propriedade='" + propriedade + '\'' +
                ", valor='" + valor + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Configuracao)) return false;

        Configuracao that = (Configuracao) o;

        if (propriedade != null ? !propriedade.equals(that.propriedade) : that.propriedade != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return propriedade != null ? propriedade.hashCode() : 0;
    }
}


package br.com.bittrexanalizer.domain;

import java.io.Serializable;

/**
 * Classe abstract onde que todos os dominios herdam
 */
public abstract class EntidadeDomain implements Serializable{

}


package br.com.bittrexanalizer.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by PauLinHo on 11/01/2018.
 */

public class MarketHistory {

    /*[{
        "Id" : 319435,
                "TimeStamp" : "2014-07-09T03:21:20.08",
                "Quantity" : 0.30802438,
                "Price" : 0.01263400,
                "Total" : 0.00389158,
                "FillType" : "FILL",
                "OrderType" : "BUY"*/

    private Long id;
    private Ticker ticker;
    private Date timeStamp;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal total;
    private String fillType;
    private String orderType;

    public MarketHistory(){

        timeStamp = new Date();
        ticker = new Ticker();
    }

    public MarketHistory(Long id, Date timeStamp, BigDecimal quantity,
                         BigDecimal price, BigDecimal total, String fillType, String orderType) {
        this.id = id;
        this.timeStamp = timeStamp;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
        this.fillType = fillType;
        this.orderType = orderType;
    }

    public MarketHistory(Long id, Ticker ticker, Date timeStamp, BigDecimal quantity,
                         BigDecimal price, BigDecimal total, String fillType, String orderType) {
        this.id = id;
        this.ticker = ticker;
        this.timeStamp = timeStamp;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
        this.fillType = fillType;
        this.orderType = orderType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getFillType() {
        return fillType;
    }

    public void setFillType(String fillType) {
        this.fillType = fillType;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "MarketHistory{" +
                "id=" + id +
                ", ticker=" + ticker +
                ", timeStamp=" + timeStamp +
                ", quantity=" + quantity +
                ", price=" + price +
                ", total=" + total +
                ", fillType='" + fillType + '\'' +
                ", orderType='" + orderType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketHistory)) return false;

        MarketHistory that = (MarketHistory) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 04/02/2018.
 */

public class data {

    private String __RequestVerificationToken;

    public String get__RequestVerificationToken() {
        return __RequestVerificationToken;
    }

    public void set__RequestVerificationToken(String __RequestVerificationToken) {
        this.__RequestVerificationToken = __RequestVerificationToken;
    }
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 02/02/2018.
 */

enum TymeInEffectType {

    GOOD_TIL_CANCELED("GOOD_TIL_CANCELED"),
    IMMEDIATE("IMMEDIATE OR CANCEL");


    private String condition;

    private TymeInEffectType(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}


package br.com.bittrexanalizer.domain;

import java.util.LinkedList;

import br.com.bittrexanalizer.webserver.HttpClient;

/**
 * Created by PauLinHo on 09/01/2018.
 */

public class AbstractTicker extends EntidadeDomain implements Runnable {

    private static LinkedList<Ticker> tickers;

    public AbstractTicker(){
        tickers = new LinkedList<>();
    }

    public static LinkedList<Ticker> getTickers() {
        return tickers;
    }

    public static void setTickers(LinkedList<Ticker> tickers) {
        AbstractTicker.tickers = tickers;
    }

    private synchronized void atualizarTicker(Ticker ticker){

        Ticker t = new Ticker();
        t = (Ticker) ticker;
        ticker = HttpClient.find(ticker.getUrlApi());

        if(ticker!=null) {
            t.setLast(ticker.getLast());
            t.setAsk(ticker.getAsk());
            t.setBid(ticker.getBid());
        }

        tickers.add(t);

    }

    @Override
    public void run() {

      //  atualizarTicker((Ticker) this);

    }
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 04/02/2018.
 */

public class OrderHistory extends EntidadeDomain {

    // POST https://bittrex.com/api/v2.0/auth/market/TradeBuy with data { MarketName: "BTC-DGB,
    // OrderType:"LIMIT", Quantity: 10000.02, Rate: 0.0000004, TimeInEffect:"GOOD_TIL_CANCELED",
    // ConditionType: "NONE", Target: 0, __RequestVerificationToken: "HIDDEN_FOR_PRIVACY"}

    /*MarketName:string, OrderType:string, Quantity:float,
    Rate:float, TimeInEffect:string,ConditionType:string,
    Target:int __RequestVerificationToken:string*/

    private String MarketName;
    private String OrderType;
    private float Quantity;
    private float Rate;
    private String TimeInEffect;
    private String ConditionType;
    private int Target;
    private String _RequestVerificationToken;

    public String getMarketName() {
        return MarketName;
    }

    public void setMarketName(String marketName) {
        MarketName = marketName;
    }

    public String getOrderType() {
        return OrderType;
    }

    public void setOrderType(String orderType) {
        OrderType = orderType;
    }

    public float getQuantity() {
        return Quantity;
    }

    public void setQuantity(float quantity) {
        Quantity = quantity;
    }

    public float getRate() {
        return Rate;
    }

    public void setRate(float rate) {
        Rate = rate;
    }

    public String getTimeInEffect() {
        return TimeInEffect;
    }

    public void setTimeInEffect(String timeInEffect) {
        TimeInEffect = timeInEffect;
    }

    public String getConditionType() {
        return ConditionType;
    }

    public void setConditionType(String conditionType) {
        ConditionType = conditionType;
    }

    public int getTarget() {
        return Target;
    }

    public void setTarget(int target) {
        Target = target;
    }

    public String get_RequestVerificationToken() {
        return _RequestVerificationToken;
    }

    public void set_RequestVerificationToken(String _RequestVerificationToken) {
        this._RequestVerificationToken = _RequestVerificationToken;
    }
}


package br.com.bittrexanalizer.domain;

import android.support.annotation.NonNull;

import static android.R.attr.value;

/**
 * Created by PauLinHo on 11/01/2018.
 */

public class Trader implements Comparable<Trader>{

    private Long id;
    private String nome;
    private String email;
    private String telefone;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trader)) return false;

        Trader trader = (Trader) o;

        if (id != null ? !id.equals(trader.id) : trader.id != null) return false;
        return email != null ? email.equals(trader.email) : trader.email == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Trader{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Trader trader) {
        int valor = (this.getId() > trader.getId()) ? 1 : 0;
        return valor;
    }
}


package br.com.bittrexanalizer.domain;

import java.math.BigDecimal;

public class Balance extends EntidadeDomain implements Comparable<Balance>{

	/*"Currency" : "DOGE",
	"Balance" : 0.00000000,
	"Available" : 0.00000000,
	"Pending" : 0.00000000,
	"CryptoAddress" : "DLxcEt3AatMyr2NTatzjsfHNoB9NT62HiF",
	"Requested" : false,
	"Uuid" : null*/

	private Long id;
	private String currency;
	private BigDecimal balance;
	private BigDecimal available;
	private BigDecimal pending;
	private String cryptoAddress;
	private Boolean requested;
	private String uuid;
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public BigDecimal getAvailable() {
		return available;
	}
	public void setAvailable(BigDecimal available) {
		this.available = available;
	}
	public BigDecimal getPending() {
		return pending;
	}
	public void setPending(BigDecimal pending) {
		this.pending = pending;
	}
	public String getCryptoAddress() {
		return cryptoAddress;
	}
	public void setCryptoAddress(String cryptoAddress) {
		this.cryptoAddress = cryptoAddress;
	}
	public Boolean getRequested() {
		return requested;
	}
	public void setRequested(Boolean requested) {
		this.requested = requested;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Balance other = (Balance) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Balance [currency=" + currency + ", balance=" + balance + ", available=" + available + ", pending="
				+ pending + ", cryptoAddress=" + cryptoAddress + ", requested=" + requested + ", uuid=" + uuid + "]";
	}
	@Override
	public int compareTo(Balance o) {
		
		int valor = 0;
		
		if(this.getAvailable().compareTo(o.getAvailable())==1){
			valor=1;
		}else if(this.getAvailable().compareTo(o.getAvailable())==-1){
			valor = -1;
		}
		return valor;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 02/02/2018.
 */

enum OrderType {

    LIMIT("LIMIT"),
    CONDITIONAL("CONDITIONAL");


    private String condition;

    private OrderType(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}


package br.com.bittrexanalizer.domain;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import br.com.bittrexanalizer.services.TickerService;

/**
 * Classe que representa as moedas que o sistema carrega assim que inicia
 * sera localiza os valores principais da moeda
 * isBought significa se foi comprada ou não
 */
public class Ticker extends TickerService implements Comparable<Ticker> {


    private Long id;
    private String nomeExchange;
    private String sigla;
    private String urlApi;
    /**
     * Indicação de Venda
     */
    private BigDecimal last;
    private BigDecimal bid;
    /**
     * Indicação de Compra
     */
    private BigDecimal ask;
    private Boolean isBought;

    private BigDecimal avisoBuyInferior;
    private BigDecimal avisoBuySuperior;
    private BigDecimal avisoStopLoss;
    private BigDecimal avisoStopGain;
    private BigDecimal valorDeCompra;


    public Ticker() {

        last = BigDecimal.ZERO;
        ask = BigDecimal.ZERO;
        bid = BigDecimal.ZERO;

        isBought = false;
        avisoBuyInferior = BigDecimal.ZERO;
        avisoBuySuperior = BigDecimal.ZERO;
        avisoStopLoss = BigDecimal.ZERO;
        avisoStopGain = BigDecimal.ZERO;
        setValorDeCompra(BigDecimal.ZERO);
        nomeExchange = "";
        sigla = "";


    }

    public String getNomeExchange() {
        return nomeExchange;
    }

    public void setNomeExchange(String nomeExchange) {
        this.nomeExchange = nomeExchange.toUpperCase();
    }

    public String getUrlApi() {
        return urlApi;
    }

    public void setUrlApi(String urlApi) {
        this.urlApi = urlApi;
    }

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla.toUpperCase();
    }

    public BigDecimal getAvisoBuyInferior() {
        return avisoBuyInferior;
    }

    public void setAvisoBuyInferior(BigDecimal avisoBuyInferior) {
        this.avisoBuyInferior = avisoBuyInferior;
    }

    public BigDecimal getAvisoBuySuperior() {
        return avisoBuySuperior;
    }

    public void setAvisoBuySuperior(BigDecimal avisoBuySuperior) {
        this.avisoBuySuperior = avisoBuySuperior;
    }

    public BigDecimal getAvisoStopLoss() {
        return avisoStopLoss;
    }

    public void setAvisoStopLoss(BigDecimal avisoStopLoss) {
        this.avisoStopLoss = avisoStopLoss;
    }

    public BigDecimal getAvisoStopGain() {
        return avisoStopGain;
    }

    public void setAvisoStopGain(BigDecimal avisoStopGain) {
        this.avisoStopGain = avisoStopGain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticker)) return false;

        Ticker ticker = (Ticker) o;

        return sigla.equals(ticker.sigla);

    }

    @Override
    public int hashCode() {
        return sigla.hashCode();
    }

    @Override
    public int compareTo(@NonNull Ticker ticker) {
        int value = this.getNomeExchange().compareTo(ticker.getNomeExchange());
        return value;
    }

    public Boolean getBought() {
        return isBought;
    }

    public void setBought(Boolean bought) {
        isBought = bought;
    }

    @Override
    public String toString() {
        return "Ticker{" +
                "id=" + id +
                ", nomeExchange='" + nomeExchange + '\'' +
                ", sigla='" + sigla + '\'' +
                ", urlApi='" + urlApi + '\'' +
                ", last=" + last +
                ", isBought=" + isBought +
                ", bid=" + bid +
                ", ask=" + ask +
                ", avisoBuyInferior=" + avisoBuyInferior +
                ", avisoBuySuperior=" + avisoBuySuperior +
                ", avisoStopLoss=" + avisoStopLoss +
                ", avisoStopGain=" + avisoStopGain +
                '}';
    }


    public BigDecimal getValorDeCompra() {
        return valorDeCompra;
    }

    public void setValorDeCompra(BigDecimal valorDeCompra) {
        this.valorDeCompra = valorDeCompra;
    }
}


package br.com.bittrexanalizer.domain;

import java.math.BigDecimal;
import java.util.Calendar;

import br.com.bittrexanalizer.services.CandleService;

public class Candle extends CandleService implements Comparable<Candle> {

	/*BV: 13.14752793,          // base volume
    C: 0.000121,              // close
	H: 0.00182154,            // high
	L: 0.0001009,             // low
	O: 0.00182154,            // open
	T: "2017-07-16T23:00:00", // timestamp
	V: 68949.3719684          // 24h volume*/

    /**
     * BASE VOLUME
     */
    private BigDecimal BV = new BigDecimal("0");
    /**
     * CLOSE
     */
    private BigDecimal C;
    /**
     * HIGH
     */
    private BigDecimal H;
    /**
     * LOW
     */
    private BigDecimal L;
    /**
     * OPEN
     */
    private BigDecimal O;
    /**
     * TIMESTAMP
     */
    private Calendar T = Calendar.getInstance();
    /**
     * VOLUME
     */
    private BigDecimal V;

    private String sigla;

    public Candle(){

    }

    public Candle(Double C, Double L, Double H){
        this.setC(new BigDecimal(C));
        this.setL(new BigDecimal(L));
        this.setH(new BigDecimal(H));
    }

    public BigDecimal getBV() {
        return BV;
    }

    public void setBV(BigDecimal bV) {
        BV = bV;
    }

    public BigDecimal getC() {
        return C;
    }

    public void setC(BigDecimal c) {
        C = c;
    }

    public BigDecimal getH() {
        return H;
    }

    public void setH(BigDecimal h) {
        H = h;
    }

    public BigDecimal getL() {
        return L;
    }

    public void setL(BigDecimal l) {
        L = l;
    }

    public BigDecimal getO() {
        return O;
    }

    public void setO(BigDecimal o) {
        O = o;
    }

    public Calendar getT() {
        return T;
    }

    public void setT(Calendar t) {
        T = t;
    }

    public BigDecimal getV() {
        return V;
    }

    public void setV(BigDecimal v) {
        V = v;
    }

    @Override
    public String toString() {
        return "Candle [BV=" + BV + ", C=" + C + ", H=" + H + ", L=" + L + ", O=" + O + ", T=" + T + ", V=" + V + "]";
    }

    @Override
    public int compareTo(Candle o) {
        int value = 0;

        if (this.T.compareTo(o.T) == 1)
            value = 1;
        else if (this.T.compareTo(o.T) == -1)
            value = -1;

        return value;
    }


    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }
}


package br.com.bittrexanalizer.domain;

/**
 * Created by PauLinHo on 02/02/2018.
 */

enum ConditionType {

    NONE("NONE"),
    GREATER("GREATER THAN OR EQUAL TO"),
    LESS("LESS THAN OR EQUAL TO");


    private String condition;

    private ConditionType(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}


package br.com.bittrexanalizer.helpers;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 08/10/2017.
 */

public class TelaHelper {

    private static Ticker ticker;
    /**
     * Get data for WebService
     */
    public static boolean getTicker() {

        ticker = new Ticker();

        try {
            LinkedList<Ticker> tickers = new LinkedList<>();

            SessionUtil.getInstance().setTickers(tickers);


        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Simula um venda de BiTCoin
     *
     * @param valorEmBitCoin
     * @return - Um BigDecimal com o valor da venda
     */
    public static BigDecimal calcularVenda(Double valorEmBitCoin, BigDecimal taxaSell, Double TAXA_SELL) {

        BigDecimal sellTemp = taxaSell;
        Double doublePrecoSell = new Double(sellTemp.toString());

        BigDecimal totalSellReais;
        Double doubleTotalSellReais;

        doubleTotalSellReais = valorEmBitCoin * doublePrecoSell;
        doubleTotalSellReais -= doubleTotalSellReais * TAXA_SELL;

        DecimalFormat df = new DecimalFormat("#.##");
        String retorno = df.format(new Double(doubleTotalSellReais.toString()));

        totalSellReais = new BigDecimal(retorno.replace(",", "."));

        return totalSellReais;

    }

    public static String calcularCompra(BigDecimal valorEmReais, BigDecimal taxaBuy, Double TAXA_BUY) {

        if (!(valorEmReais.compareTo(new BigDecimal(0.0)) == 1)) {
            return "0.00000";
        }

        BigDecimal buyTemp = taxaBuy;
        Double doublePrecoBuy = new Double(buyTemp.toString());

        DecimalFormat df = new DecimalFormat("#.#####");
        Double doubleValorEmReais = new Double(valorEmReais.toString());

        Double totalBuyBitcoin;

        totalBuyBitcoin = doubleValorEmReais / doublePrecoBuy;
        totalBuyBitcoin -= totalBuyBitcoin * TAXA_BUY;

        String retorno = df.format(totalBuyBitcoin).replace(",", ".");

        return retorno;

    }


    /**
     *
     * @param valorAplicado
     * @return
     */
    public static BigDecimal calcularTaxaEDepositoNegocieCoins(BigDecimal valorAplicado) {

        DecimalFormat df = new DecimalFormat("#.##");

        Double TAXA_DEPOSITO = 0.009d;

        Double doubleValorAplicado = new Double(valorAplicado.toString());
        Double resultado = (doubleValorAplicado * TAXA_DEPOSITO);
        BigDecimal valorDepositoCobrado = new BigDecimal("1.50");

        resultado = doubleValorAplicado - resultado;
        resultado = resultado - new Double(valorDepositoCobrado.toString());

        String retorno = df.format(resultado).replace(",", ".");

        return new BigDecimal(retorno);

    }

    /**
     *
     * @param valorAplicado
     * @return
     */
    public static BigDecimal calcularTaxaSaqueEDepositoBitCoinToYou(BigDecimal valorAplicado) {

        DecimalFormat df = new DecimalFormat("#.##");

        Double TAXA_DEPOSITO = 0.0189d;

        Double doubleValorAplicado = new Double(valorAplicado.toString());
        Double resultado = (doubleValorAplicado * TAXA_DEPOSITO);

        resultado = doubleValorAplicado - resultado;

        String retorno = df.format(resultado).replace(",", ".");

        return new BigDecimal(retorno);

    }

}


package br.com.bittrexanalizer.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.domain.EntidadeDomain;
import br.com.bittrexanalizer.facade.CompraFacade;
import br.com.bittrexanalizer.strategy.AlarmAnalizerCompraStrategy;
import br.com.bittrexanalizer.strategy.CandleStrategy;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.SessionUtil;
import br.com.bittrexanalizer.utils.WebServiceUtil;
import br.com.bittrexanalizer.webserver.HttpClient;

/**
 * Created by PauLinHo on 21/01/2018.
 */

public class CandleService extends EntidadeDomain implements IService {

    private LinkedList<Candle> candles;
    private volatile static Map<String, LinkedList<Candle>> mapCandles;


    public CandleService() {
        mapCandles = Collections.synchronizedMap(new HashMap<String, LinkedList<Candle>>());
    }


    private synchronized void buscarCandles(Candle candle) {

        Candle c = new Candle();
        c = candle;

        LinkedList<Candle> candles = new LinkedList<>();

        String url = WebServiceUtil.construirURLTicker(WebServiceUtil.getUrlTicks(),
                c.getSigla(), SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.PERIODICIDADE));
        String dados = HttpClient.find(url, null);
        if (dados != null) {
            if (WebServiceUtil.verificarRetorno(dados)) {
                candles = new CandleStrategy().getObjects(dados);
                if (AlarmAnalizerCompraStrategy.mapCandles != null) {
                    AlarmAnalizerCompraStrategy.mapCandles.put(c.getSigla(), candles);
                }

                if (CompraFacade.mapCandles != null) {
                    CompraFacade.mapCandles.put(c.getSigla(), candles);
                }

            }
        }
    }

    @Override
    public void run() {

        buscarCandles((Candle) this);

    }

    public LinkedList<Candle> getCandles() {
        return candles;
    }

    public void setCandles(LinkedList<Candle> candles) {
        this.candles = candles;
    }

    public static Map<String, LinkedList<Candle>> getMapCandles() {
        return CandleService.mapCandles;
    }

    public static void setMapCandles(Map<String, LinkedList<Candle>> mapCandles) {
        CandleService.mapCandles = mapCandles;
    }

}


package br.com.bittrexanalizer.services;

import java.util.LinkedList;

import br.com.bittrexanalizer.domain.EntidadeDomain;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.webserver.HttpClient;

/**
 * Created by PauLinHo on 21/01/2018.
 */

public class TickerService extends EntidadeDomain implements IService  {

    private LinkedList<Ticker> tickers;


    public TickerService(){
        tickers = new LinkedList<>();
    }

    public LinkedList<Ticker> getTickers() {
        return tickers;
    }

    public void setTickers(LinkedList<Ticker> tickers) {
        this.tickers = tickers;
    }

    private synchronized void atualizarTicker(Ticker ticker){

        Ticker t = new Ticker();
        t = (Ticker) ticker;
        ticker = HttpClient.find(ticker.getUrlApi());

        if(ticker!=null) {
            t.setLast(ticker.getLast());
            t.setAsk(ticker.getAsk());
            t.setBid(ticker.getBid());

        }

        tickers.add(t);

    }

    @Override
    public void run() {

        atualizarTicker((Ticker)this);

    }

}


package br.com.bittrexanalizer.services;

/**
 * Created by PauLinHo on 21/01/2018.
 */

public interface IService extends Runnable {
}


package br.com.bittrexanalizer.strategy;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 31/01/2018.
 */

public class SellOrderStrategy {

    private static boolean hasCanceled = false;

    public static boolean execute(final Order order){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //API_KEY&market=BTC-LTC&quantity=1.2&rate=


                String url = WebServiceUtil.construirURLTickerSellv1(order.getSigla(),
                        order.getQuantity().toString(),
                        order.getRate().toString());

                if (url.length() < 1) {
                    hasCanceled = false;
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, "HmacSHA512");

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

                if (!WebServiceUtil.verificarRetorno(dados)) {
                    hasCanceled = false;
                }else{
                    hasCanceled =true;
                }
            }
        });
        t.start();

        return hasCanceled;
    }
}


package br.com.bittrexanalizer.strategy;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Candle;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class CandleStrategy implements IStrategy<Candle> {

    private LinkedList<Candle> objetos;

    @Override
    public LinkedList<Candle> getObjects(String dados) {

        objetos = new LinkedList<>();

        dados = dados.replace("}", "");

        String[] dadosTemp = dados.split("\\{");

        int i = 0;
        //Se tiver muitos candles ele filtra só os ultimos 100
        if(dadosTemp.length>250) {
            i = dadosTemp.length - 110;
        }

        for (; i < dadosTemp.length; i++) {
            objetos.add(getCandle(dadosTemp[i]));
        }

        return objetos;
    }


    private synchronized static Candle getCandle(String dados) {

        Candle c = new Candle();

        String[] dadosTemp = dados.split(",");

        for (String s : dadosTemp) {

            String key[] = s.split(":");

            switch (key[0].replace("\"", "")) {
                case "O":
                    c.setO(new BigDecimal(key[1]));
                    break;
                case "H":
                    c.setH(new BigDecimal(key[1]));
                    break;
                case "L":
                    c.setL(new BigDecimal(key[1]));
                    break;
                case "C":
                    c.setC(new BigDecimal(key[1]));
                    break;
                case "V":
                    c.setV(new BigDecimal(key[1]));
                    break;
                case "T":
                    String dataTemp = key[1].replace("T", " ").replace("-", "/").replace("\"", "");
                    dataTemp = dataTemp.substring(0, 11);

                    try {
                        c.getT().setTime(SDF_DDMMYYYY.parse(dataTemp));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    break;
                case "BV":
                    c.setBV(new BigDecimal(key[1].replace("]", "")));
                    break;
            }

        }

        return c;
    }
}


package br.com.bittrexanalizer.strategy;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 31/01/2018.
 */

public class CancelOrderStrategy {

    private static boolean hasCanceled = false;

    public static boolean execute(final Order order){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                String url = WebServiceUtil.addNonce(WebServiceUtil.getUrlOrderCancel(), order.getOrderUuid());


                if (url.length() < 1) {
                    hasCanceled = false;
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, EncryptionUtility.algorithmUsed);

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);


                if (!WebServiceUtil.verificarRetorno(dados)) {
                    hasCanceled = false;
                }else{
                    hasCanceled =true;
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return hasCanceled;
    }
}


package br.com.bittrexanalizer.strategy;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public interface IStrategy<T extends Object> {

    static final String BITCOIN_BASE64 = "1NwaKCqfb532vVRFmBAixPbGQJhTfwpbzk";
    static final String BITCOIN = "BITCOIN";
    SimpleDateFormat SDF_DDMMYYYY_HHMMSS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    SimpleDateFormat SDF_DDMMYYYY = new SimpleDateFormat("yyyy/MM/dd");


    LinkedList<T> getObjects(String dados);
}


package br.com.bittrexanalizer.strategy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.utils.SessionUtil;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 04/02/2018.
 */

/**
 * Executa uma busca em todas as orderns abertas
 */
public class OpenOrderStrategy {

    private static boolean retorno = false;
    /**
     * Executo o método de busca
     *
     * @return - True se ocorreu sem erro
     *         - False se ocorreu algum erro
     */
    public static boolean execute() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                LinkedList<Order> orders = new LinkedList<>();

                String url = WebServiceUtil.addNonce(WebServiceUtil.getUrlOpenOrders());

                if (url.length() < 1) {
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, EncryptionUtility.algorithmUsed);

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

                if (!WebServiceUtil.verificarRetorno(dados)) {
                    return;
                } else {
                    orders = new OrderStrategy().getObjects(dados);
                }

                if (orders.size() == 0) {
                    retorno = true;
                    return;
                }

                //atualiza a lista de ordens abertas
                Map<String, Order> mapOpenOrders = new HashMap<>();
                for (Order o : orders) {
                    mapOpenOrders.put(o.getSigla(), o);
                }


                SessionUtil.getInstance().setMapOpenOrders(mapOpenOrders);

                retorno = true;

            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return  retorno;

    }
}


package br.com.bittrexanalizer.strategy;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 31/01/2018.
 */

public class BuyOrderStrategy {

    private static boolean hasCanceled = false;

    public static boolean execute(final Order order){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //API_KEY&market=BTC-LTC&quantity=1.2&rate=

//                MarketName:string, OrderType:string, Quantity:float, Rate:float,
//                TimeInEffect:string,ConditionType:string, Target:int __RequestVerificationToken:string

               // POST https://bittrex.com/api/v2.0/auth/market/TradeBuy with data { MarketName: "BTC-DGB,
                // OrderType:"LIMIT", Quantity: 10000.02, Rate: 0.0000004, TimeInEffect:"GOOD_TIL_CANCELED",
                // ConditionType: "NONE", Target: 0, __RequestVerificationToken: "HIDDEN_FOR_PRIVACY"}

                String url = WebServiceUtil.construirURLTickerBUYv1(order.getSigla(),
                        order.getQuantity().toString(),
                        order.getRate().toString());

                if (url.length() < 1) {
                    hasCanceled = false;
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, "HmacSHA512");

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

                if (!WebServiceUtil.verificarRetorno(dados)) {
                    hasCanceled = false;
                }else{
                    hasCanceled =true;
                }
            }
        });
        t.start();

        return hasCanceled;
    }
}


package br.com.bittrexanalizer.strategy;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.LinkedList;

import br.com.bittrexanalizer.domain.Order;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class OrderStrategy implements IStrategy<Order> {

    private LinkedList<Order> objetos;


    @Override
    public LinkedList<Order> getObjects(String dados) {

        objetos = new LinkedList<>();


        dados = dados.replace("}", "");

        String[] dadosTemp = dados.split("\\{");

        for (int i = 2; i < dadosTemp.length; i++) {

            objetos.add(getOrders(dadosTemp[i]));
        }

        return objetos;
    }

    private static Order getOrders(String dados) {

        Order order = new Order();

        /*	"Uuid" : null,
    "OrderUuid" : "09aa5bb6-8232-41aa-9b78-a5a1093e0211",
	"Exchange" : "BTC-LTC",
	"OrderType" : "LIMIT_SELL",
	"Quantity" : 5.00000000,
	"QuantityRemaining" : 5.00000000,
	"Limit" : 2.00000000,
	"CommissionPaid" : 0.00000000,
	"Price" : 0.00000000,
	"PricePerUnit" : null,
	"Opened" : "2014-07-09T03:55:48.77",
	"Closed" : null,
	"CancelInitiated" : false,
	"ImmediateOrCancel" : false,
	"IsConditional" : false,
	"Condition" : null,
	"ConditionTarget" : null*/

        String[] dadosTemp = dados.split(",");

        for (String s : dadosTemp) {

            String key[] = s.replace("]", "").split(":");

            switch (key[0].replace("\"", "").replace("\"", "")) {
                case "Uuid":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setUuid("");
                    else
                        order.setUuid(key[1].replace("\"", ""));
                    break;

                case "AccountId":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setAccountId(null);
                    else
                        order.setAccountId(new Long(key[1].replace("\"", "")));
                    break;

                case "OrderUuid":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setOrderUuid("");
                    else
                        order.setOrderUuid(key[1].replace("\"", ""));
                    break;
                case "Exchange":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setExchange("");
                    else {
                        order.setExchange(key[1].replace("\"", ""));
                        String[] sigla = order.getExchange().split("-");
                        order.setSigla(sigla[1]);
                    }
                    break;
                case "OrderType":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setOrderType("");
                    else
                        order.setOrderType(key[1].replace("\"", ""));
                    break;
                case "Type":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setType("");
                    else
                        order.setType(key[1].replace("\"", ""));
                    break;
                case "Quantity":
                    if (key[1].equals("0.00000000"))
                        order.setQuantity(new BigDecimal("0.0"));
                    else
                        order.setQuantity(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "QuantityRemaining":
                    if (key[1].equals("0.00000000"))
                        order.setQuantityRemaining(new BigDecimal("0.0"));
                    else
                        order.setQuantityRemaining(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Limit":
                    if (key[1].equals("0.00000000"))
                        order.setLimit(new BigDecimal("0.0"));
                    else
                        order.setLimit(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Reserved":
                    if (key[1].equals("0.00000000"))
                        order.setReserved(new BigDecimal("0.0"));
                    else
                        order.setReserved(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "ReserveRemaining":
                    if (key[1].equals("0.00000000"))
                        order.setReserveRemaining(new BigDecimal("0.0"));
                    else
                        order.setReserveRemaining(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Comission":
                    if (key[1].equals("0.00000000"))
                        order.setComission(new BigDecimal("0.0"));
                    else
                        order.setComission(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "CommissionReserved":
                    if (key[1].equals("0.00000000"))
                        order.setComissionPaid(new BigDecimal("0.0"));
                    else
                        order.setComissionPaid(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "CommissionReservedRemaining":
                    if (key[1].equals("0.00000000"))
                        order.setComissionPaid(new BigDecimal("0.0"));
                    else
                        order.setComissionPaid(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "CommissionPaid":
                    if (key[1].equals("0.00000000"))
                        order.setComissionPaid(new BigDecimal("0.0"));
                    else
                        order.setComissionPaid(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Price":
                    if (key[1].equals("0.00000000"))
                        order.setPrice(new BigDecimal("0.0"));
                    else
                        order.setPrice(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "PricePerUnit":
                    if (key[1].toLowerCase().replace("\"", "").equals("null") || key[1].equals("0.00000000"))
                        order.setPricePerUnit(BigDecimal.ZERO);
                    else
                        order.setPricePerUnit(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Opened":

                    if (key[1].toString().toLowerCase().replace("\"", "").equals("null"))
                        order.setOpened(null);
                    else {

                        StringBuilder data = new StringBuilder();
                        data.append(key[1].trim().replace("T", " "));
                        data.append(":");
                        data.append(key[2].trim());
                        data.append(":");
                        String dataSplit[] = key[3].trim().replace(".",":").split(":");
                        data.append(dataSplit[0]);

                        String dataFinal = data.toString().replace("\"", "").replace("-","/");

                        try {
                            order.getOpened().setTime(SDF_DDMMYYYY_HHMMSS.parse(dataFinal));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "Closed":

                    if (key[1].toString().toLowerCase().replace("\"", "").equals("null"))
                        order.setClosed(null);
                    else {

                        StringBuilder dataClosed = new StringBuilder();
                        dataClosed.append(key[1].trim().replace("T", " "));
                        dataClosed.append(":");
                        dataClosed.append(key[2].trim());
                        dataClosed.append(":");
                        String dataSplitClosed[] = key[3].trim().replace(".",":").split(":");
                        dataClosed.append(dataSplitClosed[0]);

                        String dataFinal = dataClosed.toString().replace("\"", "").replace("-","/");

                        try {
                            order.getClosed().setTime(SDF_DDMMYYYY_HHMMSS.parse(dataFinal));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "TimeStamp":

                    if (key[1].toString().toLowerCase().replace("\"", "").equals("null"))
                        order.setTimeStamp(null);
                    else {

                        StringBuilder dataClosed = new StringBuilder();
                        dataClosed.append(key[1].trim().replace("T", " "));
                        dataClosed.append(":");
                        dataClosed.append(key[2].trim());
                        dataClosed.append(":");
                        String dataSplitClosed[] = key[3].trim().replace(".",":").split(":");
                        dataClosed.append(dataSplitClosed[0]);

                        String dataFinal = dataClosed.toString().replace("\"", "").replace("-","/");

                        try {
                            order.getTimeStamp().setTime(SDF_DDMMYYYY_HHMMSS.parse(dataFinal));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case "CancelInitiated":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setCancelInitiated(false);
                    else
                        order.setCancelInitiated(Boolean.valueOf(key[1].replace("\"", "")));
                    break;
                case "ImmediateOrCancel":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setImmediateOrCancel(false);
                    else
                        order.setImmediateOrCancel(Boolean.valueOf(key[1].replace("\"", "")));
                    break;
                case "IsConditional":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setIsConditional(false);
                    else
                        order.setIsConditional(Boolean.valueOf(key[1].replace("\"", "")));
                    break;
                case "Condition":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setCondition("");
                    else
                        order.setCondition(key[1].replace("\"", ""));
                    break;
                case "ConditionTarget":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        order.setConditionTarget("");
                    else
                        order.setConditionTarget(key[1].replace("\"", ""));
                    break;

            }

        }

        return order;
    }
}


package br.com.bittrexanalizer.strategy;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Order;
import br.com.bittrexanalizer.domain.OrderHistory;
import br.com.bittrexanalizer.domain.data;
import br.com.bittrexanalizer.utils.WebServiceUtil;
import br.com.bittrexanalizer.webserver.HttpClient;

/**
 * Created by PauLinHo on 31/01/2018.
 */

public class StopOrderStrategy {

    private static boolean hasCanceled = false;

    public static boolean execute(final Order order) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                // POST https://bittrex.com/api/v2.0/auth/market/TradeBuy with data { MarketName: "BTC-DGB,
                // OrderType:"LIMIT", Quantity: 10000.02, Rate: 0.0000004, TimeInEffect:"GOOD_TIL_CANCELED",
                // ConditionType: "NONE", Target: 0, __RequestVerificationToken: "HIDDEN_FOR_PRIVACY"}

    /*MarketName:string, OrderType:string, Quantity:float,
    Rate:float, TimeInEffect:string,ConditionType:string,
    Target:int __RequestVerificationToken:string*/

                String url = WebServiceUtil.constuirgetUrlOrderHistoryV2_0();
                OrderHistory orderHistory = new OrderHistory();

                String hash = EncryptionUtility.calculateHash(url, EncryptionUtility.algorithmUsed);
                orderHistory.set_RequestVerificationToken(hash);

                data data = new data();
                data.set__RequestVerificationToken(EncryptionUtility.generateNonce());



                String dados = HttpClient.findPost(WebServiceUtil.getUrlOrderHistoryV20(),hash, data);



                if (url.length() < 1) {
                    hasCanceled = false;
                    return;
                }


//                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

            }
        });
        t.start();

        return hasCanceled;
    }
}


package br.com.bittrexanalizer.strategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import br.com.bittrexanalizer.api.EncryptionUtility;
import br.com.bittrexanalizer.domain.Balance;
import br.com.bittrexanalizer.utils.SessionUtil;
import br.com.bittrexanalizer.utils.WebServiceUtil;

/**
 * Created by PauLinHo on 17/01/2018.
 */

public class BalanceStrategy implements IStrategy<Balance> {

    private LinkedList<Balance> objetos;

    private static boolean retorno = false;


    /**
     * Executo o método de busca
     *
     * @return - True se ocorreu sem erro
     * - False se ocorreu algum erro
     */
    public static boolean execute() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {


                LinkedList<Balance> balances = new LinkedList<>();

                String url = WebServiceUtil.addNonce(WebServiceUtil.getUrlBalanceApyKey());

                if (url.length() < 1) {
                    return;
                }

                String hash = EncryptionUtility.calculateHash(url, EncryptionUtility.algorithmUsed);

                String dados = br.com.bittrexanalizer.webserver.HttpClient.find(url, hash);

                if (dados == null) {
                    return;
                }
                if (!WebServiceUtil.verificarRetorno(dados)) {
                    return;
                } else {
                    balances = new BalanceStrategy().getObjects(dados);

                }
                if (balances.size() == 0) {
                    retorno = true;
                    return;
                }

                retorno = true;

            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return retorno;
    }


    @Override
    public LinkedList<Balance> getObjects(String dados) {

        objetos = new LinkedList<>();

        Map<String, Balance> mapBalances = new HashMap<>();

        dados = dados.replace("}", "");

        String[] dadosTemp = dados.split("\\{");

        for (int i = 2; i < dadosTemp.length; i++) {
            Balance balance = getBalances(dadosTemp[i]);

            if (balance.getAvailable().compareTo(BigDecimal.ZERO) == 1 ||
                    balance.getBalance().compareTo(BigDecimal.ZERO) == 1) {
                objetos.add(balance);
                mapBalances.put(balance.getCurrency(), balance);
            }

        }

        SessionUtil.getInstance().setMapBalances(mapBalances);

        return objetos;
    }

    private synchronized static Balance getBalances(String dados) {

        Balance balance = new Balance();

        /*"Currency" : "DOGE",
    "Balance" : 0.00000000,
	"Available" : 0.00000000,
	"Pending" : 0.00000000,
	"CryptoAddress" : "DLxcEt3AatMyr2NTatzjsfHNoB9NT62HiF",
	"Requested" : false,
	"Uuid" : null*/

        String[] dadosTemp = dados.split(",");

        for (String s : dadosTemp) {

            String key[] = s.replace("]", "").split(":");

            switch (key[0].replace("\"", "").replace("\"", "")) {
                case "Currency":
                    balance.setCurrency(key[1].replace("\"", ""));

                    break;
                case "Balance":
                    if (key[1].equals("0.00000000"))
                        balance.setBalance(new BigDecimal("0.0"));
                    else
                        balance.setBalance(new BigDecimal(key[1]));
                    break;
                case "Available":
                    if (key[1].equals("0.00000000"))
                        balance.setAvailable(new BigDecimal("0.0"));
                    else
                        balance.setAvailable(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "Pending":
                    if (key[1].equals("0.00000000"))
                        balance.setPending(new BigDecimal("0.0"));
                    else
                        balance.setPending(new BigDecimal(key[1].replace("\"", "")));
                    break;
                case "CryptoAddress":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        balance.setCryptoAddress("");
                    else
                        balance.setCryptoAddress(key[1].replace("\"", ""));
                    break;
                case "Request":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        balance.setRequested(false);
                    else
                        balance.setRequested(Boolean.valueOf(key[1].replace("\"", "")));
                    break;
                case "Uuid":
                    if (key[1].toLowerCase().replace("\"", "").equals("null"))
                        balance.setUuid("");
                    else
                        balance.setUuid(key[1].replace("\"", ""));
                    break;
            }

        }

        return balance;
    }
}


package br.com.bittrexanalizer.strategy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.bittrexanalizer.R;
import br.com.bittrexanalizer.analises.IAnaliser;
import br.com.bittrexanalizer.analises.IFRAnaliser;
import br.com.bittrexanalizer.analises.MACDAnaliser;
import br.com.bittrexanalizer.analises.OBVAnaliser;
import br.com.bittrexanalizer.analises.OsciladorEstocasticoAnaliser;
import br.com.bittrexanalizer.database.dao.ConfiguracaoDAO;
import br.com.bittrexanalizer.domain.Candle;
import br.com.bittrexanalizer.domain.Configuracao;
import br.com.bittrexanalizer.domain.Ticker;
import br.com.bittrexanalizer.telas.MainActivityDrawer;
import br.com.bittrexanalizer.utils.ConstantesUtil;
import br.com.bittrexanalizer.utils.EmailUtil;
import br.com.bittrexanalizer.utils.SessionUtil;

/**
 * Created by PauLinHo on 16/09/2017.
 */

public class AlarmAnalizerCompraStrategy {

    private Context context;
    private boolean devoParar = false;
    public static Map<String, LinkedList<Candle>> mapCandles;
    private StringBuilder textoNotificacao;

    private StringBuilder moedasPositivasParaCompra = new StringBuilder();
    private StringBuilder moedasNegativasParaCompra = new StringBuilder();
    private StringBuilder moedasErros = new StringBuilder();

    private MACDAnaliser macdAnaliser;
    private IFRAnaliser ifrAnaliser;
    private OsciladorEstocasticoAnaliser osciladorEstocasticoAnaliser;
    private OBVAnaliser obvAnaliser;
    private EmailUtil emailUtil;

    private boolean verificarMACD = true;
    private boolean verificarIFR = true;
    private boolean verificarOE = true;
    private boolean verificarOBV = true;

    private int contador = 0;
    private int qtdeTickersPesquisar;
    private int tempoEsperoThread;

    private LinkedList<Ticker> tickers;

    public void executar(Context context) {

        this.context = context;

        moedasPositivasParaCompra.append("\n\rMOEDAS POSITIVAS PARA COMPRAR SEGUNDO AS ANALISES SOLICITADAS: \r\n");
        moedasNegativasParaCompra.append("\n\r\rMOEDAS NEGATIVAS PARA COMPRAR SEGUNDO AS ANALISES SOLICITADAS: \r\n");
        moedasErros.append("\r\r\rEXISTEM ERROS: \r\n");
        textoNotificacao = new StringBuilder();
        textoNotificacao.append("Moedas positivadas para Compra: \n");

        //Atualiza as configurações
        getConfiguracoes();

        qtdeTickersPesquisar = Integer.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.QTDE_TICKERS_PESQUISA));
        tempoEsperoThread = Integer.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.TEMPO_ESPERA_THREAD));

        SessionUtil.getInstance().setMaxCandleParaPesquisar(0);

        macdAnaliser = new MACDAnaliser();
        ifrAnaliser = new IFRAnaliser();
        osciladorEstocasticoAnaliser = new OsciladorEstocasticoAnaliser();
        obvAnaliser = new OBVAnaliser();
        emailUtil = new EmailUtil();

        verificarMACD = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.MACD));
        verificarIFR = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.IFR));
        verificarOE = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OSCILADOR_ESTOCASTICO));
        verificarOBV = Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OBV));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                do {

                    mapCandles = new ConcurrentHashMap<>(new HashMap<String, LinkedList<Candle>>());

                    getDados();

                    Set<String> keys = null;
                    if (mapCandles != null) {
                        keys = mapCandles.keySet();
                    }


                    for (String k : keys) {
                        Log.i("BIITREX", k);
                        contador++;

                        LinkedList<Candle> lista = mapCandles.get(k);
                        if (lista.size() > 0) {
                            realizarAnalises(k, lista);

                        }
                    }

                    try {
                        if(!devoParar) {
                            Thread.sleep(tempoEsperoThread);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } while (!devoParar);


                moedasPositivasParaCompra.append("\r\nFORAM ANALISADAS " + contador + " MOEDAS\n");
                moedasPositivasParaCompra.append("\r\n\nMACD: " + String.valueOf(verificarMACD).toUpperCase() + "\n");
                moedasPositivasParaCompra.append("\r\n\nIFR: " + String.valueOf(verificarIFR).toUpperCase() + " - VALOR: " +
                        SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.IFR_MIN));
                moedasPositivasParaCompra.append("\r\n\nOSCILADOR ESTOCASTICO: " + String.valueOf(verificarOE).toUpperCase() + " - VALOR: " +
                        SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OE_TAXA_MIN));
                moedasPositivasParaCompra.append("\r\n\nOBV: " + String.valueOf(verificarOBV).toUpperCase() + " - QTDE_FECHAMENTOS: " +
                        SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.OBV_QTDE_FECHAMENTOS));


                if (SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ENVIAR_EMAIL)) {
                    if (Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ENVIAR_EMAIL))) {

                        StringBuilder textoEmail = new StringBuilder();
                        textoEmail.append(moedasPositivasParaCompra.toString());
                        textoEmail.append("\r\n");
                        textoEmail.append(moedasNegativasParaCompra.toString());
                        textoEmail.append("\r\n");
                        textoEmail.append(moedasErros.toString());

                        enviarEmail(textoEmail.toString(), "INFORMAÇÃO");
                    }
                }


                contador = 0;
                moedasPositivasParaCompra = new StringBuilder();
                moedasNegativasParaCompra = new StringBuilder();

            }
        });
        t.start();


    }

    public void realizarAnalises(String sigla, LinkedList<Candle> candles) {

        boolean devoComprar = false;

        try {
            Log.i("Sigla", sigla);
            if (verificarMACD) {
                int valorMACD = macdAnaliser.analizer(candles);

                if (valorMACD != IAnaliser.IDEAL_PARA_COMPRA) {
                    devoComprar = false;
                } else {
                    devoComprar = true;
                }

            }

            if (verificarIFR) {
                int valorIFR = ifrAnaliser.analizer(candles);

                if (valorIFR != IAnaliser.IDEAL_PARA_COMPRA) {
                    devoComprar = false;
                } else {
                    devoComprar = true;
                }

            }

            if (verificarOE) {
                int valorOE = osciladorEstocasticoAnaliser.analizer(candles);

                if (valorOE != IAnaliser.IDEAL_PARA_COMPRA) {
                    devoComprar = false;
                } else {
                    devoComprar = true;
                }

            }

            if (verificarOBV) {
                int valorOBV = obvAnaliser.analizer(candles);

                if (valorOBV != IAnaliser.IDEAL_PARA_COMPRA) {
                    devoComprar = false;
                } else {
                    devoComprar = true;
                }

            }


            if (devoComprar) {

                //String texto = TEXTO_EMAIL_COMPRA + sigla;

                textoNotificacao.append("\t"+sigla);

                moedasPositivasParaCompra.append("\r\t");
                moedasPositivasParaCompra.append(sigla);
                moedasPositivasParaCompra.append("\t");

            } else {

                moedasPositivasParaCompra.append("\r\t");
                moedasNegativasParaCompra.append(sigla);
                moedasNegativasParaCompra.append("\t");
            }

        } catch (Exception e) {
            moedasErros.append("\t\r");
            moedasErros.append(e.getMessage());

        }

    }

    private void enviarEmail(final String mensagem, final String operacao) {

        if (SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ENVIAR_NOTIFICACAO)) {

            if (Boolean.valueOf(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ENVIAR_NOTIFICACAO))) {
                criarNotificacao(context, textoNotificacao.toString(), operacao);
            }
        }
        emailUtil.enviarEmail(context, mensagem, operacao);
    }

    public void getDados() {

        try {

            Boolean isAllTickers = false;
            /**
             * Faz a verificação se foi selecionados todas as moedas
             * ou se será calculado apenas nas moedas que o usuario esta analizando
             */
            if (SessionUtil.getInstance().getMapConfiguracao().containsKey(ConstantesUtil.ALL_TICKERS)) {

                isAllTickers = new Boolean(SessionUtil.getInstance().getMapConfiguracao().get(ConstantesUtil.ALL_TICKERS));

                if (isAllTickers) {
                    tickers = new LinkedList<>();
                    Set<String> keys = SessionUtil.getInstance().getNomeExchanges().keySet();

                    for (String k : keys) {
                        Ticker t = new Ticker();

                        t.setSigla(k);

                        tickers.add(t);
                    }
                } else {
                    tickers = SessionUtil.getInstance().getTickers();
                }

            } else {
                tickers = SessionUtil.getInstance().getTickers();
            }


            ExecutorService executorService = Executors.newCachedThreadPool();
            int flagParar = 0;
            int i = 0;



            if ((SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar) > tickers.size()) {
                i = SessionUtil.getInstance().getMaxCandleParaPesquisar();
                flagParar = tickers.size();
                SessionUtil.getInstance().setMaxCandleParaPesquisar(Integer.MIN_VALUE);
                devoParar = true;
            } else {
                flagParar = SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar;
                i = SessionUtil.getInstance().getMaxCandleParaPesquisar();
            }

            for (; i < tickers.size(); i++) {

                if (i == flagParar) {
                    SessionUtil.getInstance().setMaxCandleParaPesquisar(SessionUtil.getInstance().getMaxCandleParaPesquisar() + qtdeTickersPesquisar);
                    break;
                }

                Candle candle = new Candle();
                candle.setSigla(tickers.get(i).getSigla());


                executorService.execute(candle);
            }

            executorService.shutdown();

            while (!executorService.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            moedasErros.append("\r\n");
            moedasErros.append(e.getMessage());
            return;

        }
    }

    /**
     * Atualiza as configurações, pois se foi alterado algum parametro
     * será atualizado
     */
    private void getConfiguracoes() {

        LinkedList<Configuracao> configuracoes = new LinkedList<>();
        configuracoes = new ConfiguracaoDAO(context).all();

        Map<String, String> mapConfiguracao = new HashMap<String, String>();

        if (configuracoes == null) {
            SessionUtil.getInstance().setMapConfiguracao(null);
            return;
        }

        for (Configuracao c : configuracoes) {
            mapConfiguracao.put(c.getPropriedade(), c.getValor());
        }

        SessionUtil.getInstance().setMapConfiguracao(mapConfiguracao);


    }

    /**
     * Criar uma notificaçao para exibir
     *
     * @param context
     */

    private void criarNotificacao(Context context, String texto, String operacao) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivityDrawer.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.bittrexanalizer)
                .setTicker("Aviso BITTREXANALIZER")
                .setContentTitle("Aviso BITTREXANALIZER")
                .setContentText(texto + operacao)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        Notification not = builder.build();
        not.vibrate = new long[]{150, 100, 6000, 100};
        not.flags = Notification.FLAG_AUTO_CANCEL;


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, not);

    }


}


