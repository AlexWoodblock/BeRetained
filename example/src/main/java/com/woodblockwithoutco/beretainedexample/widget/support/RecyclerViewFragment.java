/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package com.woodblockwithoutco.beretainedexample.widget.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woodblockwithoutco.beretainedexample.R;

/**
 * Simple Fragment that contains RecyclerView with simple TextView items with data
 * from CharSequence array.
 */
public class RecyclerViewFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private CharSequence[] mItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context parentContext = container.getContext();
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mRecyclerView = (RecyclerView) v;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(parentContext));
        SimpleAdapter adapter = new SimpleAdapter();
        mRecyclerView.setAdapter(adapter);

        if(mItems != null) {
            adapter.setItems(mItems);
        }

        return v;
    }



    public void setItems(CharSequence[] items) {
        mItems = items;

        if(mRecyclerView != null) {
            SimpleAdapter adapter = (SimpleAdapter) mRecyclerView.getAdapter();
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    }

    static class SimpleAdapter extends RecyclerView.Adapter<SimpleHolder> {

        private CharSequence[] mItems;

        public void setItems(CharSequence[] items) {
            mItems = items;
        }

        @Override
        public SimpleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.simple_recyclerview_item, parent, false);
            SimpleHolder holder = new SimpleHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(SimpleHolder holder, int position) {
            holder.getTextView().setText(mItems[position]);
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.length : 0;
        }
    }

    static class SimpleHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public SimpleHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        public TextView getTextView() {
            return mTextView;
        }

    }
}
