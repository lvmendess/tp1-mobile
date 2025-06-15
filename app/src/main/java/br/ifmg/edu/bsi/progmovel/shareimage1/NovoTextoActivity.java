package br.ifmg.edu.bsi.progmovel.shareimage1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NovoTextoActivity extends AppCompatActivity {

    public static String EXTRA_TEXTO_CIMA_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.texto_cima_atual";
    public static String EXTRA_TEXTO_BAIXO_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.texto_cima_atual";
    public static String EXTRA_COR_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.cor_atual";
    public static String EXTRA_TAM_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.tam_atual";
    public static String EXTRA_NOVO_TEXTO_CIMA = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_texto_cima";
    public static String EXTRA_NOVO_TEXTO_BAIXO = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_texto_baixo";
    public static String EXTRA_NOVA_COR = "br.ifmg.edu.bsi.progmovel.shareimage1.nova_cor";
    public static String EXTRA_NOVO_TAM = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_tam";

    private EditText etTextoCima;
    private EditText etTextoBaixo;
    private EditText etCor;
    private EditText etTam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_texto);

        etTextoCima = findViewById(R.id.etTextoCima);
        etTextoBaixo = findViewById(R.id.etTextoBaixo);
        etCor = findViewById(R.id.etCor);
        etTam = findViewById(R.id.etTam);

        Intent intent = getIntent();
        String textoCimaAtual = intent.getStringExtra(EXTRA_TEXTO_CIMA_ATUAL);
        String textoBaixoAtual = intent.getStringExtra(EXTRA_TEXTO_BAIXO_ATUAL);
        String corAtual = intent.getStringExtra(EXTRA_COR_ATUAL);
        String tamAtual = intent.getStringExtra(EXTRA_TAM_ATUAL);
        etTextoCima.setText(textoCimaAtual);
        etTextoBaixo.setText(textoBaixoAtual);
        etCor.setText(corAtual);
        etTam.setText(tamAtual);
    }

    public void enviarNovoTexto(View v) {
        String novoTextoCima = etTextoCima.getText().toString();
        String novoTextoBaixo = etTextoBaixo.getText().toString();
        String novaCor = etCor.getText().toString();
        String novoTam = etTam.getText().toString();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NOVO_TEXTO_CIMA, novoTextoCima);
        intent.putExtra(EXTRA_NOVO_TEXTO_BAIXO, novoTextoBaixo);
        intent.putExtra(EXTRA_NOVA_COR, novaCor);
        intent.putExtra(EXTRA_NOVO_TAM, novoTam);
        setResult(RESULT_OK, intent);
        finish();
    }
}