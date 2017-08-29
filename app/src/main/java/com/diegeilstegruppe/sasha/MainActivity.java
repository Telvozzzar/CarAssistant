package com.diegeilstegruppe.sasha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diegeilstegruppe.sasha.Audio.WavAudioRecorder;
import com.diegeilstegruppe.sasha.Spotify.SpotifyController;
import com.diegeilstegruppe.sasha.witAi.WitAiApiAccess;
import com.diegeilstegruppe.sasha.witAi.SearchQuery;
import com.diegeilstegruppe.sasha.Spotify.SpotifySearchAdapter;
import com.diegeilstegruppe.sasha.Services.HeadSetMonitoring.HeadsetMonitoringService;
import com.diegeilstegruppe.sasha.Services.Notifications.BusProvider;
import com.diegeilstegruppe.sasha.witAi.WitAiResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit2.Response;

import static android.speech.RecognizerIntent.EXTRA_RESULTS;
import static com.diegeilstegruppe.sasha.R.id.album_image;
import static com.diegeilstegruppe.sasha.R.id.tv_search_query;
import static com.diegeilstegruppe.sasha.R.id.tv_track_name;


public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

	private static final String TAG = "MainActivity";
	private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
	private static final int SPEECHRECOGNITION_REQUESTCODE = 42;
	private final int SPOTIFY_LOGIN_REQUESTCODE = 1337;
	private boolean permissionToRecordAccepted = false;
	private WavAudioRecorder wavAudioRecorder;
	private WitAiApiAccess witAiApiAccess;
	private SpotifyController spotifyController;
	//for the searchView
	private RecyclerView recyclerView;
	private RecyclerView.Adapter searchAdapter;
	private ArrayList<Parcelable> results = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


			//start headsetService
			Intent i = new Intent("initialiseHeadsetService");
			i.setClass(this, HeadsetMonitoringService.class);
			this.startService(i);

			//search results
			recyclerView = (RecyclerView) findViewById(R.id.searchResults);
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
			recyclerView.setItemAnimator(new DefaultItemAnimator());

			//request permissions
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.RECORD_AUDIO},
					REQUEST_RECORD_AUDIO_PERMISSION);

			//init waveRecorder
			final String mFileName = getCacheDir().getAbsolutePath() + "/audio.wav";
			wavAudioRecorder = WavAudioRecorder.getInstance();

			//init spotifyController Player
			spotifyController = SpotifyController.getInstance(this, this, this, this);
			spotifyController.logintoSpotify();

			//init bus for ServerResponses
			BusProvider.getInstance().register(this);

			//init Layout
			final Button recordButton = (Button) findViewById(R.id.btn_record);

			recordButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (spotifyController.isPlaying())
						spotifyController.pause();
					Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
					startActivityForResult(speechRecognitionIntent, SPEECHRECOGNITION_REQUESTCODE);
				}
			});
			final ImageButton nextButton = (ImageButton) findViewById(R.id.btn_next_track);
			nextButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					spotifyController.skipToNext();
				}
			});

			final ImageButton previousButton = (ImageButton) findViewById(R.id.btn_previous_track);
			previousButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					spotifyController.skipToPrevious();
				}
			});

			final ImageButton playButton = (ImageButton) findViewById(R.id.btn_play_pause);
			playButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (spotifyController.isPlaying())
						spotifyController.pause();
					else
						spotifyController.resume();
				}
			});

			/**
			 * DEPRICATED!!
			 * This is only for voice recording and sending this record to wit.ai.
			 * Slower and also requires to keep the button pressed while recording...
			 */
	        /*button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Pressed
                        TextView tv = (TextView) findViewById(tv_search_query);
                        tv.setText("");
                        spotifyController.record(wavAudioRecorder, mFileName);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Released
                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.stop();
                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        wavAudioRecorder.reset();
                        Log.d("State WaveRecorder: ", wavAudioRecorder.getState().toString());
                        witAiApiAccess = new WitAiApiAccess();

                        File file = new File(mFileName);
                        witAiApiAccess.uploadFile(file);
                        spotifyController.resumeIfWasPlaying();
                    }
                    return true;
                }
            });*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Spotify.destroyPlayer(this);
		super.onDestroy();
	}

	//BusListeners
	@Subscribe
	public void resultsReady(TracksPager results) {
		this.results.clear();
		for (Track t : results.tracks.items) {
			this.results.add(t);
		}
		searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
		recyclerView.setAdapter(searchAdapter);
	}

	@Subscribe
	public void onPagerPost(Pager pager) {
		if (!pager.items.isEmpty()) {
			Object firstElemet = pager.items.iterator().next();
			if (firstElemet instanceof PlaylistSimple) {
				Pager<PlaylistSimple> results = pager;
				for (PlaylistSimple pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof TrackSimple) {
				Pager<TrackSimple> results = pager;
				for (TrackSimple pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof AlbumSimple) {
				Pager<AlbumSimple> results = pager;
				for (AlbumSimple pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof ArtistSimple) {
				Pager<ArtistSimple> results = pager;
				for (ArtistSimple pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof Track) {
				Pager<Track> results = pager;
				for (Track pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof Album) {
				Pager<Album> results = pager;
				for (Album pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof Artist) {
				Pager<Artist> results = pager;
				for (Artist pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof Playlist) {
				Pager<Playlist> results = pager;
				for (Playlist pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof SavedAlbum) {
				Pager<SavedAlbum> results = pager;
				for (SavedAlbum pl : results.items) {
					this.results.add(pl);
				}
			} else if (firstElemet instanceof SavedTrack) {
				Pager<SavedTrack> results = pager;
				for (SavedTrack pl : results.items) {
					this.results.add(pl);
				}
			}

			searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
			recyclerView.setAdapter(searchAdapter);
		}
	}

	@Subscribe
	public void onPlaylistPost(PlaylistsPager playlists) {
		this.results.clear();
		for (PlaylistSimple pl : playlists.playlists.items) {
			this.results.add(pl);
		}
		searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
		recyclerView.setAdapter(searchAdapter);
	}

	@Subscribe
	public void onAlbumPost(AlbumsPager albums) {
		this.results.clear();
		for (AlbumSimple pl : albums.albums.items) {
			this.results.add(pl);
		}
		searchAdapter = new SpotifySearchAdapter(this.results, getApplicationContext());
		recyclerView.setAdapter(searchAdapter);
	}

	@Subscribe
	public void answerAvailable(Response<WitAiResponse> event) {
		TextView tv = (TextView) findViewById(tv_search_query);
		String query = null;
		String intent = null;
		if (event.body().getEntities().getSearchQuery() != null) {
			query = "";
			for (SearchQuery s : event.body().getEntities().getSearchQuery()) {
				query += " " + s.getValue();

			}
		}
		if (event.body().getEntities().getIntent() != null) {
			intent = event.body().getEntities().getIntent().iterator().next().getValue();
		}
		switch (intent) {
			case "logout":
				tv.setText(intent);
				spotifyController.logout();
				break;
			case "login":
				tv.setText(intent);
				spotifyController.logintoSpotify();
				break;
			case "play":
				tv.setText(intent + ": " + query);
				spotifyController.searchAndPlaySong(query);
				break;
			case "pause":
				tv.setText(intent);
				spotifyController.pause();
				break;
			case "skip":
				tv.setText(intent);
				spotifyController.skipToNext();
				break;
			case "addToQueue":
				tv.setText(intent + ": " + query);
				spotifyController.searchAndQueueSong(query);
				break;
			case "resume":
				tv.setText(intent);
				spotifyController.resume();
				break;
			case "searchTrack":
				this.results.clear();
				tv.setText(intent + ": " + query);
				spotifyController.searchTracks(query);
				break;
			case "searchPlaylist":
				this.results.clear();
				tv.setText(intent + ": " + query);
				spotifyController.searchPlaylists(query);
				break;
			case "searchAlbum":
				this.results.clear();
				tv.setText(intent + ": " + query);
				spotifyController.searchAlbum(query);
				break;
			case "searchArtist":
				this.results.clear();
				tv.setText(intent + ": " + query);
				spotifyController.searchArtist(query);
				break;
			case "showMyTracks":
				this.results.clear();
				tv.setText(intent);
				for (int i = 0; i < 10; i++)
					spotifyController.showMyTracks(i * 50);
				break;
			case "showMyAlbums":
				this.results.clear();
				tv.setText(intent);
				for (int i = 0; i < 10; i++)
					spotifyController.showMyAlbums(i * 50);
				break;
			case "showMyPlaylists":
				this.results.clear();
				tv.setText(intent);
				for (int i = 0; i < 10; i++)
					spotifyController.showMyPlaylists(i * 50);
				break;
			default:
				Toast toast = Toast.makeText(getApplicationContext(), "No Intent found!", Toast.LENGTH_LONG);
				toast.show();
				return;
		}
	}

	@Override
	public void onPlaybackEvent(PlayerEvent playerEvent) {
		Log.d("MainActivity", "Playback event received: " + playerEvent.name());
		switch (playerEvent) {
			// Handle event type as necessary
			case kSpPlaybackNotifyAudioDeliveryDone:
				Log.d("onPlayBackEvent", "kSpPlayBackNotifyAudioDelivery");
				spotifyController.skipToNext();
			case kSpPlaybackNotifyMetadataChanged:
				Metadata metadata = spotifyController.getSpotifyPlayer().getMetadata();
				ImageView iv = (ImageView) findViewById(album_image);
				TextView tv = (TextView) findViewById(tv_track_name);
				tv.setText(metadata.currentTrack.name);
				String url = metadata.currentTrack.albumCoverWebUrl;
				if (url != null) {
					Picasso.with(this).load(url).into(iv);
				}

			default:
				break;
		}
	}

	@Override
	public void onPlaybackError(Error error) {
		Log.d("MainActivity", "Playback error received: " + error.name());
		switch (error) {
			default:
				break;
		}
	}

	@Override
	public void onLoggedIn() {
		spotifyController.loggedin();
	}

	@Override
	public void onLoggedOut() {
		Log.d("MainActivity", "User logged out");
	}

	@Override
	public void onLoginFailed(Error error) {
		Log.d("MainActivity", "Login failed");
	}

	@Override
	public void onTemporaryError() {
		Log.d("MainActivity", "Temporary error occurred");
	}

	@Override
	public void onConnectionMessage(String message) {
		Log.d("MainActivity", "Received connection message: " + message);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == SPOTIFY_LOGIN_REQUESTCODE && resultCode == RESULT_OK)
			spotifyController.startPlayer(requestCode, resultCode, intent);
		if (requestCode == SPEECHRECOGNITION_REQUESTCODE && resultCode == RESULT_OK) {
			spotifyController.resumeIfWasPlaying();
			witAiApiAccess = new WitAiApiAccess();
			witAiApiAccess.sendText(intent.getStringArrayListExtra(EXTRA_RESULTS).get(0));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_RECORD_AUDIO_PERMISSION:
				permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				break;
		}
		if (!permissionToRecordAccepted) {
			finish();
		}
	}
}
