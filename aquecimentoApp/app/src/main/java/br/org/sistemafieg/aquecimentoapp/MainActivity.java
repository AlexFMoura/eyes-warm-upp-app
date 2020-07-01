package br.org.sistemafieg.aquecimentoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import br.org.sistemafieg.aquecimentoapp.fragments.confdispositivos.ConfDispositivosFragment;
import br.org.sistemafieg.aquecimentoapp.fragments.confexercicio.ConfExercicioFragment;
import br.org.sistemafieg.aquecimentoapp.fragments.confpadraotreinamento.ConfPadraoTreinamentoFragment;
import br.org.sistemafieg.aquecimentoapp.fragments.execucaotreinamento.ExecutarTreinamentoFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_execucao_treinamento) {
            resetFragment();

            ExecutarTreinamentoFragment confDispositivosFragment = new ExecutarTreinamentoFragment();
            //confExercicioFragment.setTitle(String.valueOf(item.getTitle()));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, confDispositivosFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_conf_exercicio) {
            resetFragment();

            ConfExercicioFragment confExercicioFragment = new ConfExercicioFragment();
            //confExercicioFragment.setTitle(String.valueOf(item.getTitle()));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, confExercicioFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_conf_padrao_treinamento) {
            resetFragment();

            ConfPadraoTreinamentoFragment confPadraoTreinamentoFragment = new ConfPadraoTreinamentoFragment();
            //confExercicioFragment.setTitle(String.valueOf(item.getTitle()));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, confPadraoTreinamentoFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_conf_dispositivos) {
            resetFragment();

            ConfDispositivosFragment confExercicioFragment = new ConfDispositivosFragment();
            //confExercicioFragment.setTitle(String.valueOf(item.getTitle()));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, confExercicioFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void resetFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(currentFragment != null) {
            currentFragment.onDestroy();
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        }
    }

}
