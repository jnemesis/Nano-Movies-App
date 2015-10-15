package com.otatech.android.nanomoviesapp.movies.utilities;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.otatech.android.nanomoviesapp.movies.R;

import java.util.List;

public class Adapters extends BaseAdapter {

    private final Context context;
    private final LayoutInflater layInflater;

    private final Movie movLock = new Movie();

    private List<Movie> lstMovieObjs;

    public Adapters(Context context, List<Movie> lstMovieObjs) {
        this.context = context;
        layInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.lstMovieObjs = lstMovieObjs;
    }

    public Context getContext() {
        return context;
    }

    public void add(Movie movObj) {
        synchronized (movLock) {
            lstMovieObjs.add(movObj);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (movLock) {
            lstMovieObjs.clear();
        }
        notifyDataSetChanged();
    }

    public void setData(List<Movie> lstMovies) {
        clear();
        for (Movie movie : lstMovies) {
            add(movie);
        }
    }

    @Override
    public int getCount() {
        return lstMovieObjs.size();
    }

    @Override
    public Movie getItem(int intPos) {
        return lstMovieObjs.get(intPos);
    }

    @Override
    public long getItemId(int intPos) {
        return intPos;
    }

    @Override
    public View getView(int intPos, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = layInflater.inflate(R.layout.grid_item_movie, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        final Movie movie = getItem(intPos);

        String strCover = "http://image.tmdb.org/t/p/w185" + movie.getStrCover();

        viewHolder = (ViewHolder) view.getTag();

        Glide.with(getContext()).load(strCover).into(viewHolder.ivImage);
        viewHolder.tvTitle.setText(movie.getStrTitle());

        return view;
    }

    public static class ViewHolder {
        public final ImageView ivImage;
        public final TextView tvTitle;

        public ViewHolder(View view) {
            ivImage = (ImageView) view.findViewById(R.id.grid_item_image);
            tvTitle = (TextView) view.findViewById(R.id.grid_item_title);
        }
    }

    public static class ReviewAdapter extends BaseAdapter {

        private final Context context;
        private final LayoutInflater loInflater;
        private final Movie.Review movReview = new Movie.Review();

        private List<Movie.Review> lstReviews;

        public ReviewAdapter(Context context, List<Movie.Review> lstReviews) {
            this.context = context;
            loInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.lstReviews = lstReviews;
        }

        public Context getContext() {
            return context;
        }

        public void add(Movie.Review revObj) {
            synchronized (movReview) {
                lstReviews.add(revObj);
            }
            notifyDataSetChanged();
        }

        public void clear() {
            synchronized (movReview) {
                lstReviews.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return lstReviews.size();
        }

        @Override
        public Movie.Review getItem(int intPos) {
            return lstReviews.get(intPos);
        }

        @Override
        public long getItemId(int intPos) {
            return intPos;
        }

        @Override
        public View getView(int intPos, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;

            if (view == null) {
                view = loInflater.inflate(R.layout.item_movie_review, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }

            final Movie.Review movReview = getItem(intPos);

            viewHolder = (ViewHolder) view.getTag();

            viewHolder.tvAuthor.setText(movReview.getStrWriter());
            viewHolder.tvContent.setText(Html.fromHtml(movReview.getStrJibberish()));

            return view;
        }

        public static class ViewHolder {
            public final TextView tvAuthor;
            public final TextView tvContent;

            public ViewHolder(View view) {
                tvAuthor = (TextView) view.findViewById(R.id.review_author);
                tvContent = (TextView) view.findViewById(R.id.review_content);
            }
        }

    }

    public static class TrailerAdapter extends BaseAdapter {

        private final Context context;
        private final LayoutInflater loInflater;
        private final Movie.Trailer movTrailer = new Movie.Trailer();

        private List<Movie.Trailer> lstTrailers;

        public TrailerAdapter(Context context, List<Movie.Trailer> objects) {
            this.context = context;
            loInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            lstTrailers = objects;
        }

        public Context getContext() {
            return context;
        }

        public void add(Movie.Trailer object) {
            synchronized (movTrailer) {
                lstTrailers.add(object);
            }
            notifyDataSetChanged();
        }

        public void clear() {
            synchronized (movTrailer) {
                lstTrailers.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return lstTrailers.size();
        }

        @Override
        public Movie.Trailer getItem(int intPos) {
            return lstTrailers.get(intPos);
        }

        @Override
        public long getItemId(int intPos) {
            return intPos;
        }

        @Override
        public View getView(int intPos, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;

            if (view == null) {
                view = loInflater.inflate(R.layout.item_movie_trailer, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }

            final Movie.Trailer movTrailer = getItem(intPos);

            viewHolder = (ViewHolder) view.getTag();

            String strTrailerImgURL = "http://img.youtube.com/vi/" + movTrailer.getStrKey() + "/0.jpg";
            Glide.with(getContext()).load(strTrailerImgURL).into(viewHolder.imgView);

            return view;
        }

        public static class ViewHolder {
            public final ImageView imgView;
            public ViewHolder(View view) {
                imgView = (ImageView) view.findViewById(R.id.trailer_image);
            }
        }

    }
}
