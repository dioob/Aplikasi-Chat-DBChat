package dioobanu.yahoo.dbchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


class SectionsPagerAdapter extends FragmentPagerAdapter{

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int posisi) {

        switch (posisi){
            case 0:
                /*RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:*/
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        //return 3; //ada 3 fragments
        return 1;
    }

    public CharSequence getPageTitle(int posisi){

        switch (posisi)
        {
            case 0:
                /*return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:*/
                return "DAFTAR TEMAN";
            default:
                return null;
        }
    }
}
