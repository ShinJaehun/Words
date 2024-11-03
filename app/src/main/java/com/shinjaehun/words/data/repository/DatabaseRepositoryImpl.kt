package com.shinjaehun.words.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.shinjaehun.words.data.WordsDao
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.utils.FirebaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DatabaseRepositoryImpl"

class DatabaseRepositoryImpl(
    private val wordsDao: WordsDao
) : DatabaseRepository {
    override suspend fun getListOfWords(): LiveData<List<WordEntry>> =
        withContext(Dispatchers.IO) {
            return@withContext wordsDao.getWordsList()
        }

    override suspend fun deleteAll() =
        withContext(Dispatchers.IO) {
            wordsDao.deleteAll()
        }

    override suspend fun addWord(word: WordEntry) =
        withContext(Dispatchers.IO) {
            wordsDao.insert(word)
        }

    override suspend fun deleteWord(word: WordEntry, isDelete: Boolean) =
        withContext(Dispatchers.IO) {
            if (isDelete || FirebaseUtil.uid == null){
                Log.i(TAG, "얘네는 완전히 삭제해야 함 - word: ${word.word} isDelete: $isDelete")
                wordsDao.deleteWord(word.word)
                // 졸라 개 병신 쓰레기 같은 실수 저질렀음...
                // 반드시 if else를 bracket으로 묶어라...
            } else {
                Log.i(
                    TAG,
                    "RoomDB에서는 삭제하지 않고 insert 합니다 - word: ${word.word} isDelete: $isDelete"
                ) //onConflict = OnConflictStrategy.REPLACE라서 그냥 갱신될꺼다 이건가?
                wordsDao.insert(word)
                // 근데 왜 delete()가 아닌 insert()?????
                // isAlive가 0인 상태로 저장해둔다?
//                wordsDao.deleteWord(word.word)
                // 걍 이렇게 삭제해도 상관 없는 듯함...
                // 아냐!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // 여기서 삭제해버리면
                // 나중에 sync했을때 이미 데이터가 삭제되어 있기 때문에
                // FB에 남아있는 데이터를 삭제할 수 있는 방법이 없음
                // 그래서 여기서 isAlive만 0으로 놔두고
                // commitSynchronizationAsync()에서 FB에 isAlive가 0인 자료를 먼저 삭제하고 RDB에서 다시 삭제함
            }
        }

    override suspend fun getListFromFirebase(): LiveData<List<WordEntry>> =
        withContext(Dispatchers.IO) {
            FirebaseUtil.liveDataList
        }

    override suspend fun addListOfWords(words: List<WordEntry>) =
        withContext(Dispatchers.IO) {
            wordsDao.insertList(words)
        }
}