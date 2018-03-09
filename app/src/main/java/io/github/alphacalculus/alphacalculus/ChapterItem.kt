package io.github.alphacalculus.alphacalculus

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast

class ChapterItem(val name: String, val content: String, val video: Uri, val partIdx: Int, val chapterIdx: Int, val imageId: Int, val lockedBy: Int) : Parcelable {

    val nextChapter: ChapterItem?
        get() {
            if (ChapterItemFactory.getChapterCount(this.partIdx) > this.chapterIdx + 1) {
                return ChapterItemFactory.getChapterCached(this.partIdx, this.chapterIdx + 1)
            } else {
                val context = TheApp.instance!!.applicationContext
                Toast.makeText(context, "已经是最后一章！", Toast.LENGTH_SHORT).show()
                return this
            }
        }

    val previousChapter: ChapterItem?
        get() {
            if (this.chapterIdx > 0) {
                return ChapterItemFactory.getChapterCached(this.partIdx, this.chapterIdx - 1)
            } else {
                val context = TheApp.instance!!.applicationContext
                Toast.makeText(context, "已经是第一章！", Toast.LENGTH_SHORT).show()
                return this
            }
        }

    val isReadable: Boolean
        get() = lockedBy<=-1 || QuizLogDAO.finished(lockedBy)


    // Parcelling part
    constructor(p: Parcel) : this (
            name = p.readString(),
            content = p.readString(),
            video = p.readParcelable<Uri>(ClassLoader.getSystemClassLoader()),
            imageId = p.readInt(),
            partIdx = p.readInt(),
            chapterIdx = p.readInt(),
            lockedBy = p.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(content)
        dest.writeParcelable(video, flags)
        dest.writeInt(imageId)
        dest.writeInt(partIdx)
        dest.writeInt(chapterIdx)
        if (lockedBy != null) {
            dest.writeInt(lockedBy)
        }
    }

    companion object CREATOR : Parcelable.Creator<ChapterItem>{
            override fun createFromParcel(`in`: Parcel): ChapterItem {
                return ChapterItem(`in`)
            }

            override fun newArray(size: Int): Array<ChapterItem?> {
                return arrayOfNulls(size)
            }
        }

}
