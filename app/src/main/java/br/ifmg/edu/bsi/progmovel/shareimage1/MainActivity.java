package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity que cria uma imagem com um texto e imagem de fundo.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private GridView gridView;
    private MemeCreator memeCreator;
    private int[] templates = {R.drawable.fry_meme, R.drawable.kneesurgery,R.drawable.domingoanoite, R.drawable.mage};
    private int templateAtual = 0;
    private float[] lastTouchDownXY = new float[2];
    private final ActivityResultLauncher<Intent> startNovoTexto = registerForActivityResult(new StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            String novoTextoCima = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TEXTO_CIMA);
                            String novoTextoBaixo = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TEXTO_BAIXO);
                            String novaCor = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVA_COR);
                            String novoTam = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TAM);
                            boolean textoSuperior = intent.getBooleanExtra(NovoTextoActivity.EXTRA_TOGGLETEXTO, false);
                            if (novaCor == null) {
                                Toast.makeText(MainActivity.this, "Cor desconhecida. Usando preto no lugar.", Toast.LENGTH_SHORT).show();
                                novaCor = "BLACK";
                            }

                            memeCreator.setTextoCima(novoTextoCima);
                            memeCreator.setTextoBaixo(novoTextoBaixo);
                            memeCreator.setCorTexto(Color.parseColor(novaCor.toUpperCase()));
                            memeCreator.setTamTexto(Float.parseFloat(novoTam));
                            memeCreator.setTextoCima(textoSuperior);
                            mostrarImagem();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> startImagemFundo = registerForActivityResult(new PickVisualMedia(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result == null) {
                        return;
                    }
                    try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(result, "r")) {
                        Bitmap imagemFundo = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), result);
                        memeCreator.setFundo(imagemFundo);

                        // descobrir se é preciso rotacionar a imagem
                        FileDescriptor fd = pfd.getFileDescriptor();
                        ExifInterface exif = new ExifInterface(fd);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                            memeCreator.rotacionarFundo(90);
                        }

                        mostrarImagem();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });

    private ActivityResultLauncher<String> startWriteStoragePermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result) {
                        Toast.makeText(MainActivity.this, "Sem permissão de acesso a armazenamento do celular.", Toast.LENGTH_SHORT).show();
                    } else {
                        compartilhar(null);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        //Bitmap imagemFundo = BitmapFactory.decodeResource(getResources(), R.drawable.fry_meme);
        Bitmap imagemFundo = BitmapFactory.decodeResource(getResources(), templates[templateAtual]);

        memeCreator = new MemeCreator("Olá Android!", "Olá Android!", Color.WHITE, 64, imagemFundo, getResources().getDisplayMetrics());
        mostrarImagem();

        /*imageView.setOnLongClickListener(e -> {
            Toast.makeText(getApplicationContext(), "long CLICK", Toast.LENGTH_SHORT).show();
            Log.d("MEMECREATOR", "Long pressed");
            return true;
        });*/

        imageView.setOnTouchListener(touchlistener);
        imageView.setOnLongClickListener(longClickListener);
    }

    View.OnTouchListener touchlistener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event){
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                lastTouchDownXY[0] = event.getX();
                lastTouchDownXY[1] = event.getY();
            }
            return false;
        }
    };

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            float x = lastTouchDownXY[0];
            float y = lastTouchDownXY[1];
            if(memeCreator.isTextoCima()){
                memeCreator.setxSuperior(x);
                memeCreator.setySuperior(y);
            }else{
                memeCreator.setX(x);
                memeCreator.setY(y);
            }
            mostrarImagem();

            Log.i("tag","long click");
            Toast.makeText(getApplicationContext(), "long CLICK", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    public void iniciarMudarTexto(View v) {
        Intent intent = new Intent(this, NovoTextoActivity.class);
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_CIMA_ATUAL, memeCreator.getTextoCima());
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_BAIXO_ATUAL, memeCreator.getTextoBaixo());
        intent.putExtra(NovoTextoActivity.EXTRA_COR_ATUAL, converterCor(memeCreator.getCorTexto()));
        intent.putExtra(NovoTextoActivity.EXTRA_TAM_ATUAL, Float.toString(memeCreator.getTamTexto()));

        startNovoTexto.launch(intent);
    }

    public void iniciarMudarTemplate(View v){
        if(templateAtual+1 == templates.length){
            templateAtual = 0;
        }else{
            templateAtual++;
        }
        Bitmap imagemFundo = BitmapFactory.decodeResource(getResources(), templates[templateAtual]);
        memeCreator.setFundo(imagemFundo);
        mostrarImagem();
    }

    public String converterCor(int cor) {
        switch (cor) {
            case Color.BLACK: return "BLACK";
            case Color.WHITE: return "WHITE";
            case Color.BLUE: return "BLUE";
            case Color.GREEN: return "GREEN";
            case Color.RED: return "RED";
            case Color.YELLOW: return "YELLOW";
        }
        return null;
    }

    public void iniciarMudarFundo(View v) {
        startImagemFundo.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ImageOnly.INSTANCE)
                .build());
    }

    public void compartilhar(View v) {
        compartilharImagem(memeCreator.getImagem());
    }

    public void mostrarImagem() {
        imageView.setImageBitmap(memeCreator.getImagem());
    }

    public void compartilharImagem(Bitmap bitmap) {

        // pegar a uri da mediastore
        // pego o volume externo pq normalmente ele é maior que o volume interno.
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            /*
            Em versões <= 28, é preciso solicitar a permissão WRITE_EXTERNAL_STORAGE.
            Mais detalhes em https://developer.android.com/training/data-storage/shared/media#java.
             */
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (PackageManager.PERMISSION_GRANTED != write) {
                startWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        // montar a nova imagem a ser inserida na mediastore
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "shareimage1file");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        Uri imageUri = getContentResolver().insert(contentUri, values);

        // criar a nova imagem na pasta da mediastore
        try (
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "w");
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())
            ) {
            BufferedOutputStream bytes = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gravar imagem:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // compartilhar a imagem com intent implícito
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_TITLE, "Seu meme fabuloso");
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(share, "Compartilhar Imagem"));
    }
}
