package com.tokyoc.line_client

import android.os.Parcel
import android.os.Parcelable

// MemberデータFormat
data class Member(val name: String, val id: Int, val groupId: Int): Parcelable {
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Member> = object: Parcelable.Creator<Member> {
            override fun createFromParcel(source: Parcel): Member = source.run {
                Member(readString(), readInt(), readInt())
            }
            override fun newArray(size: Int): Array<Member?> = arrayOfNulls(size)
        }
    }
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeString(name)
            writeInt(id)
            writeInt(groupId)
        }
    }
}