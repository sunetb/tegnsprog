package dk.stbn.testts;

/**
 * Created by sune on 11/23/17.
 */

public class SkraldFraMain {

    /*
    *
    *
    * MediaSource lavKilde (Uri s){

		HttpDataSource.Factory kilde = new DefaultHttpDataSourceFactory("mig", new TransferListener<DataSource>() {
			@Override
			public void onTransferStart(DataSource source, DataSpec dataSpec) {
			}

			@Override
			public void onBytesTransferred(DataSource source, int bytesTransferred) {

			}
			@Override
			public void onTransferEnd(DataSource source) {

			}
		});

		MediaSource ms = new ExtractorMediaSource(
				s,
				kilde,
				new DefaultExtractorsFactory(), null, null);

		return ms;
	}

	//-- Til resultatlisten. Skjuler/viser pilen som angiver mere end ét resultat
/*		resultatliste.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView p1, int p2)
			{
				if(p2 == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					mere.setAlpha(0);
				}
			}

			@Override
			public void onScroll(AbsListView p1, int p2, int p3, int p4)
			{
				//Denne metode bliver kaldt hele tiden, dvs ikke kun når brugeren scroller
			}
		});

		resultatliste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != viserposition) {
					viserposition = position;
					sp.edit().putInt("position", position).commit();
					a.visPil = false;
					opdaterUI(false, a.søgeresultat.get(position).nøgle, position);
					//derBlevSøgt = true;
					t("resultatliste.onItemclick(). visPil? = "+a.visPil);

				}
			}
		});
*
*
*
*
*
*
*
*
*
*
		//-- Alternativ til at bruge knappen "Mere"
		resultatliste.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
			{
				Intent i = new Intent (MainActivity.this, FuldArtikel_akt.class);
				startActivity(i);
				return false;
			}
		});

		afsp.addListener(new ExoPlayer.EventListener() {

			@Override
			public void onTimelineChanged(Timeline timeline, Object manifest) {
				p("Timeline: "+timeline);
			}

			@Override
			public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

			}

			@Override
			public void onLoadingChanged(boolean isLoading) {

			}

			@Override
			public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

				if (playbackState == ExoPlayer.STATE_ENDED)
					if (!a.loop){
						afsp.seekTo(0);
						afsp.setPlayWhenReady(false);
					}
					else {
						afsp.seekTo(0);
						afsp.setPlayWhenReady(true);
					}
				}

			@Override
			public void onRepeatModeChanged(int repeatMode) {
				p("repeatmode ændret til: "+repeatMode);
			}

			@Override
			public void onPlayerError(ExoPlaybackException error) {

			}

			@Override
			public void onPositionDiscontinuity() {

			}

			@Override
			public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

			}




		});


    *
    *
    *
    *
    *
    *
    *
    * /*
		//-- Listen med søgeresultater
		resultaterListeAdapter = new ArrayAdapter(this, listelayout, R.id.tekst, a.søgeresultat){

			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

				View rod = super.getView(position, convertView, parent);

				p("getview resultatlisteadapter pos"+position);
				ImageView iv = (ImageView) rod.findViewById(R.id.billede);
				iv.setImageResource(R.drawable.kaffef314);
				TextView t = (TextView) rod.findViewById(R.id.tekst);
				t.setText(a.søgeresultat.get(position).toString());//søgeresultat.get(position).getTekst());
				Button b = (Button) rod.findViewById(R.id.knap);
				b.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View p1) //-- Kan også aktiveres med langt klik
						{
							Intent i = new Intent (MainActivity.this, FuldArtikel_akt.class);
							startActivity(i);
						}


					});
				return rod;
			}

			@Override
			public int getCount() {
				return a.søgeresultat.size();
			}
		};
		resultatliste.setAdapter(resultaterListeAdapter);

		//-- Den lille pil som indikerer at der er flere resultater
		;

    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    *
    * */
}
