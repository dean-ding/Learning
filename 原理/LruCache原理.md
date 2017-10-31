LruCache原理：使用LinkedHashMap存储
存储内容放在app内存中，app退出后会删除。如果需要多次使用，建议使用DiskLruCache缓存
LinkedHashMap：put时添加在map的最后面，get时将获取的项移动到最后面。以此来实现最近未使用算法。
    当添加到超过设置的缓存大小时，则删除第一个。